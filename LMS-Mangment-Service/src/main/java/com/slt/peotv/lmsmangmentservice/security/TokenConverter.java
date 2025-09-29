package com.slt.peotv.lmsmangmentservice.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.slt.peotv.lmsmangmentservice.feign_client.UserClient;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;
import io.github.resilience4j.retry.annotation.Retry;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TokenConverter {

    @Autowired
    private JwtConfiguration jwtConfiguration;
    @Autowired
    private UserClient userClient;

    private final Object tokenDecryptionLock = new Object();
    private final Object tokenValidationLock = new Object();

    private final ConcurrentHashMap<String, UserRest> tokenCache = new ConcurrentHashMap<>();


    public String decryptToken(String encryptedToken) throws ParseException, JOSEException {
        synchronized (tokenDecryptionLock) {
            JWEObject jweObject = JWEObject.parse(encryptedToken);
            DirectDecrypter directDecrypter = new DirectDecrypter(
                    jwtConfiguration.getPrivateKey().getBytes()
            );
            jweObject.decrypt(directDecrypter);
            return jweObject.getPayload().toSignedJWT().serialize();
        }
    }

    public UserRest validateTokenSignature(String signedToken, HttpServletRequest request,
                                           String originalAuthHeader) throws ParseException, JOSEException {

        UserRest cachedUser = tokenCache.get(signedToken);
        if (cachedUser != null) {
            return cachedUser;
        }

        SignedJWT signedJWT = SignedJWT.parse(signedToken);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        if (isTokenExpired(claimsSet) || !isSignatureValid(signedJWT)) {
            return null;
        }

        UserRest user = fetchUserWithResilience(
                claimsSet.getSubject(),
                originalAuthHeader,
                request
        );

        if (user == null || (!user.getUserId().equals("LMS") && !user.getUserId().equals(extractUserId(request)))) {
            return null;
        }

        assignAuthorities(user, claimsSet.getStringListClaim("authorities"));

        tokenCache.put(signedToken, user);
        return user;
    }

    private UserRest fetchUserWithResilience(String userId, String token,
                                             HttpServletRequest request) {
        synchronized (tokenValidationLock) {
            try {
                return fetchUserWithRetry(userId, token);
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "User service unavailable"
                );
            }
        }
    }

    @Retry(name = "userServiceRetry", fallbackMethod = "fetchUserFallback")
    private UserRest fetchUserWithRetry(String userId, String token) {
        return userClient.getEmployeeById(userId, token);
    }

    private UserRest fetchUserFallback(String userId, String token, Exception e) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "User service unavailable"
        );
    }

    private boolean isTokenExpired(JWTClaimsSet claimsSet) {
        return claimsSet.getExpirationTime().before(new Date());
    }

    private boolean isSignatureValid(SignedJWT signedJWT) throws JOSEException, ParseException {
        return signedJWT.verify(new RSASSAVerifier(
                RSAKey.parse(signedJWT.getHeader().getJWK().toJSONObject())
        ));
    }

    private String extractUserId(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.substring(requestURI.lastIndexOf("/") + 1);
    }

    private void assignAuthorities(UserRest user, List<String> roles) {
        if (roles != null) {
            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            user.setAuthorities(authorities);
        }
    }
}