package com.slt.peotv.lmsmangmentservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Utils {

    private final Random RANDOM = new SecureRandom();
    private final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final String ALPHANUMERIC_UPPERCASE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String NUMERIC = "0123456789";

    private static final AtomicLong counter = new AtomicLong(0);

    private final String instanceId;

    public Utils() {
        this.instanceId = Long.toHexString(System.currentTimeMillis() % 10000).toUpperCase();
    }

    public String generateUserId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateAddressId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateUniqueId(int length, IdType type) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        if (length <= 6) {
            return generateShortUniqueId(length, type);
        }

        return generateLongUniqueId(length, type);
    }

    public String generateUniqueId(int length) {
        return generateUniqueId(length, IdType.ALPHANUMERIC);
    }

    public String generateUUIDBasedId(int length) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (length <= uuid.length()) {
            return uuid.substring(0, length).toUpperCase();
        } else {
            return (uuid + generateRandomString(length - uuid.length(), ALPHANUMERIC_UPPERCASE)).toUpperCase();
        }
    }

    public String generateTimestampBasedId(int length) {
        long timestamp = System.currentTimeMillis();
        long nanos = System.nanoTime() % 1000000;
        long counterVal = counter.incrementAndGet() % 1000;

        String base = Long.toString(timestamp, 36).toUpperCase() +
                Long.toString(nanos, 36).toUpperCase() +
                Long.toString(counterVal, 36).toUpperCase();

        if (base.length() >= length) {
            return base.substring(0, length);
        } else {
            return base + generateRandomString(length - base.length(), ALPHANUMERIC_UPPERCASE);
        }
    }

    private String generateShortUniqueId(int length, IdType type) {
        long timestamp = System.currentTimeMillis() % 100000;
        long counterVal = counter.incrementAndGet() % 1000;

        String charset = getCharsetByType(type);
        String base = Long.toString(timestamp, charset.length()) +
                Long.toString(counterVal, charset.length()) +
                instanceId;

        StringBuilder result = new StringBuilder();
        for (char c : base.toCharArray()) {
            if (Character.isDigit(c)) {
                int digit = Character.getNumericValue(c);
                if (digit < charset.length()) {
                    result.append(charset.charAt(digit));
                } else {
                    result.append(charset.charAt(digit % charset.length()));
                }
            } else {
                result.append(c);
            }
        }

        String uniqueBase = result.toString();
        if (uniqueBase.length() >= length) {
            return uniqueBase.substring(0, length);
        } else {
            return uniqueBase + generateRandomString(length - uniqueBase.length(), charset);
        }
    }

    private String generateLongUniqueId(int length, IdType type) {
        long timestamp = System.currentTimeMillis();
        long counterVal = counter.incrementAndGet();
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);

        String charset = getCharsetByType(type);

        int prefixLength = Math.max(3, length / 3);
        String prefix = Long.toString(timestamp, 36).toUpperCase() +
                Integer.toString(randomPart, 36).toUpperCase() +
                Long.toString(counterVal % 1000, 36).toUpperCase();

        if (prefix.length() > prefixLength) {
            prefix = prefix.substring(0, prefixLength);
        }

        int remainingLength = length - prefix.length();
        String suffix = remainingLength > 0 ?
                generateRandomString(remainingLength, charset) : "";

        return (prefix + suffix).substring(0, length);
    }

    private String generateRandomString(int length) {
        return generateRandomString(length, ALPHABET);
    }

    private String generateRandomString(int length, String charset) {
        StringBuilder returnValue = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            returnValue.append(charset.charAt(RANDOM.nextInt(charset.length())));
        }

        return new String(returnValue);
    }

    private String getCharsetByType(IdType type) {
        return switch (type) {
            case ALPHANUMERIC -> ALPHABET;
            case ALPHANUMERIC_UPPERCASE -> ALPHANUMERIC_UPPERCASE;
            case NUMERIC -> NUMERIC;
        };
    }

    public String generateIdWithPrefix(String prefix, int totalLength) {
        if (prefix == null || prefix.isEmpty()) {
            return generateUniqueId(totalLength);
        }

        if (prefix.length() >= totalLength) {
            return prefix.substring(0, totalLength);
        }

        int remainingLength = totalLength - prefix.length();
        return prefix + generateUniqueId(remainingLength);
    }

    public String generateLeaveRequestId(int length) {
        return "LV" + generateTimestampBasedId(Math.max(1, length - 2));
    }

    public String generateEmployeeId(int length) {
        return "EMP" + generateTimestampBasedId(Math.max(1, length - 3));
    }

    public enum IdType {
        ALPHANUMERIC,
        ALPHANUMERIC_UPPERCASE,
        NUMERIC
    }

    public static boolean hasTokenExpired(String token) {
        boolean returnValue = false;

        try {
            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);

            JwtParser parser = Jwts.parser().verifyWith(key).build();

            Claims claims = parser.parseSignedClaims(token).getPayload();

            Date tokenExpirationDate = claims.getExpiration();
            Date todayDate = new Date();

            returnValue = tokenExpirationDate.before(todayDate);
        } catch (ExpiredJwtException ex) {
            returnValue = true;
        }

        return returnValue;
    }

    public String generateEmailVerificationToken(String userId) {
        return generateToken(userId);
    }

    public String generatePasswordResetToken(String userId) {
        return generateToken(userId);
    }

    private String generateToken(String userId) {
        byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId)
                .expiration(Date.from(now.plusMillis(SecurityConstants.EXPIRATION_TIME)))
                .issuedAt(Date.from(now))
                .signWith(secretKey)
                .compact();
    }
}