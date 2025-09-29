package com.slt.peotv.userservice.lms.entity;

import com.slt.peotv.userservice.lms.repository.TempUserRepo;
import javax.persistence.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Entity
@Table(name = "temp_user")
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPeoTvId() {
        return peoTvId;
    }

    public void setPeoTvId(String peoTvId) {
        this.peoTvId = peoTvId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

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
