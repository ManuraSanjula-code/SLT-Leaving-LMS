package com.slt.peotv.userservice.lms.configs;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class CustomPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            // Generate a random salt
            String salt = generateSalt();

            // Concatenate the raw password and salt
            String saltedPassword = rawPassword + salt;

            // Hash the salted password using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(saltedPassword.getBytes());

            // Encode the hash and salt in Base64
            String encodedPassword = Base64.getEncoder().encodeToString(hash);
            String encodedSalt = Base64.getEncoder().encodeToString(salt.getBytes());

            // Combine the encoded password and salt (you can use a delimiter)
            return  encodedPassword + ":" + encodedSalt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Split the encoded password and salt
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }

        String storedPasswordHash = parts[0];
        String storedSalt = new String(Base64.getDecoder().decode(parts[1]));

        // Concatenate the raw password and stored salt
        String saltedPassword = rawPassword + storedSalt;

        try {
            // Hash the salted password using SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(saltedPassword.getBytes());

            // Encode the hash in Base64
            String computedPasswordHash = Base64.getEncoder().encodeToString(hash);

            // Compare the computed hash with the stored hash
            return storedPasswordHash.equals(computedPasswordHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String generateSalt() {
        // Generate a 16-character random salt
        byte[] saltBytes = new byte[16];
        new Random().nextBytes(saltBytes);
        return new String(saltBytes);
    }
}