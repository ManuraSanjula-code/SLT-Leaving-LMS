package com.slt.peotv.userservice.lms.security.jwt.token.converter;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.slt.peotv.userservice.lms.security.jwt.property.JwtConfiguration;
import com.slt.peotv.userservice.lms.service.UserService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

@Service
public class TokenConverter {

    @Autowired
    private JwtConfiguration jwtConfiguration;

    public String decryptToken(String encryptedToken) throws ParseException, JOSEException {
        JWEObject jweObject = JWEObject.parse(encryptedToken);
        DirectDecrypter directDecrypter = new DirectDecrypter(jwtConfiguration.getPrivateKey().getBytes());
        jweObject.decrypt(directDecrypter);
        return jweObject.getPayload().toSignedJWT().serialize();
    }

    public UserDetails validateTokenSignature(UserService userService, String signedToken, HttpServletRequest request) throws ParseException, JOSEException {

        SignedJWT signedJWT = SignedJWT.parse(signedToken);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        final Date expiration = claimsSet.getExpirationTime();
        Date todayDate = new Date();

        if (expiration.before(todayDate)) {
            return null;
        }
        RSAKey publicKey = RSAKey.parse(signedJWT.getHeader().getJWK().toJSONObject());

        if (!signedJWT.verify(new RSASSAVerifier(publicKey))) {
            return null;
        }
        String userId = signedJWT.getPayload().toJSONObject().get("sub").toString();
        String requestURI = request.getRequestURI();
        String id = requestURI.substring(requestURI.lastIndexOf("/") + 1);

        String header = request.getHeader("X-User-ID");
        if (header == null || header.isEmpty()) header = userId;

        if ((Objects.equals(userId, id) || header.equals(userId))) return userService.getUserDetailsByUserId(userId);

        return userService.getUserDetailsByUserId(userId);
    }
}
