package com.slt.peotv.userservice.lms.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.slt.peotv.userservice.lms.security.SecurityConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class IdUtils {

    private final Random RANDOM = new SecureRandom();
    private final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


    public String generateId(int length) {
        return generateRandomString(length);
    }


    private String generateRandomString(int length) {
        StringBuilder returnValue = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return returnValue.toString(); // Fixed: removed unnecessary 'new String()'
    }


    public static boolean hasTokenExpired(String token) {
        boolean returnValue = false;

        try {
            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);

            // Java 8 compatible JWT parsing
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build();

            Claims claims = parser.parseClaimsJws(token).getBody();

            Date tokenExpirationDate = claims.getExpiration();
            Date todayDate = new Date();

            returnValue = tokenExpirationDate.before(todayDate);
        } catch (ExpiredJwtException ex) {
            returnValue = true;
        } catch (Exception ex) {
            // Handle other JWT exceptions (malformed, signature invalid, etc.)
            returnValue = true;
        }

        return returnValue;
    }


    public static String getUserIdFromToken(String token) {
        try {
            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);

            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build();

            Claims claims = parser.parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception ex) {
            return null;
        }
    }


    public String generateEmailVerificationToken(String userId) {
        return generateToken(userId);
    }


    public String generatePasswordResetToken(String userId) {
        return generateToken(userId);
    }


    private String generateToken(String userId) {
        try {
            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
            Instant now = Instant.now();

            return Jwts.builder()
                    .setSubject(userId)
                    .setExpiration(Date.from(now.plusMillis(SecurityConstants.EXPIRATION_TIME)))
                    .setIssuedAt(Date.from(now))
                    .signWith(secretKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }


    public static boolean isTokenValid(String token) {
        try {
            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);

            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build();

            parser.parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    public static Date getTokenExpirationDate(String token) {
        try {
            byte[] secretKeyBytes = SecurityConstants.getTokenSecret().getBytes();
            SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);

            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build();

            Claims claims = parser.parseClaimsJws(token).getBody();
            return claims.getExpiration();
        } catch (Exception ex) {
            return null;
        }
    }
}