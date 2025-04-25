package com.slt.peotv.userservice.lms.entity;

import com.slt.peotv.userservice.lms.repository.TempUserRepo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Entity
@Table(name = "temp_user")
@Data
@EqualsAndHashCode
public class TempUser implements Serializable {

    private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String peoTvId;

    private Date expireTime;

    private boolean admin;

    public class AuthService {

        private final TempUserRepo tempUserRepository;

        public AuthService(TempUserRepo tempUserRepository) {
            this.tempUserRepository = tempUserRepository;
        }

        public TempUser verifyUser(String email, String password) {
            TempUser user = tempUserRepository.findByEmail(email);

            if(user == null) {
                throw new UsernameNotFoundException("User not found: " + email);
            }

            if (user.isAdmin()) {
                verifyHashedPassword(user.getPassword(), password);
            } else {
                verifyPlainTextPassword(user.getPassword(), password);
            }
            return user;
        }

        private void verifyHashedPassword(String storedPassword, String inputPassword) {
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                throw new SecurityException("Invalid password format");
            }

            String salt = parts[0];
            String storedHash = parts[1];
            String computedHash = computeSHA512(inputPassword + salt);

            if (!computedHash.equalsIgnoreCase(storedHash)) {
                throw new SecurityException("Invalid password");
            }
        }

        private void verifyPlainTextPassword(String storedPassword, String inputPassword) {
            if (!storedPassword.equals(inputPassword)) {
                throw new SecurityException("Invalid password");
            }
        }

        private String computeSHA512(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                byte[] digest = md.digest(input.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-512 algorithm not available", e);
            }
        }
    }
}
