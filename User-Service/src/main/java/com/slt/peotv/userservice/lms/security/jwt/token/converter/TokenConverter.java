package com.slt.peotv.userservice.lms.security.jwt.token.converter;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.slt.peotv.userservice.lms.entity.TempUser;
import com.slt.peotv.userservice.lms.entity.UserEntity;
import com.slt.peotv.userservice.lms.repository.TempUserRepo;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.security.UserPrincipal;
import com.slt.peotv.userservice.lms.security.jwt.property.JwtConfiguration;
import com.slt.peotv.userservice.lms.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenConverter {

    private final JwtConfiguration jwtConfiguration;
    private final UserRepository userRepo;
    private final TempUserRepo tempUserRepo;

    public String decryptToken(String encryptedToken) throws ParseException, JOSEException {
        JWEObject jweObject = JWEObject.parse(encryptedToken);
        DirectDecrypter directDecrypter = new DirectDecrypter(jwtConfiguration.getPrivateKey().getBytes());
        jweObject.decrypt(directDecrypter);
        return jweObject.getPayload().toSignedJWT().serialize();
    }

    public String validateTokenSignature_v1(String signedToken, HttpServletRequest request) throws ParseException, JOSEException {

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
        String[] parts = signedJWT.getPayload().toJSONObject().get("sub").toString().split(" ");
        String email_ = parts[0];
        String loginType = parts.length > 1 ? parts[1] : "DEFAULT";

        String requestURI = request.getRequestURI();
        String id = requestURI.substring(requestURI.lastIndexOf("/") + 1);

        if ("TEMP".equals(loginType)) {
            TempUser tempUser = tempUserRepo.findTempUserByUserId(email_);

            if (tempUser == null) return null;

            if ((Objects.equals(tempUser.getUserId(), id))) {
                return tempUser.getUserId() + " " + "TEMP";
            }else{
                return null;
            }

        } else {
            UserEntity user = userRepo.findByUserId(email_);
            if (user != null || !(Objects.equals(user.getUserId(), id))) {
                return user.getUserId();
            }
        }
        return null;
    }

    public String validateTokenSignature(String signedToken, HttpServletRequest request) throws ParseException, JOSEException {
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

        UserEntity user = userRepo.findByUserId(signedJWT.getPayload().toJSONObject().get("sub").toString());

        String requestURI = request.getRequestURI();
        String id = requestURI.substring(requestURI.lastIndexOf("/") + 1);

        if (user == null || !(Objects.equals(user.getUserId(), id))) {
            return null;
        } else {
            return user.getUserId();
        }
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

        return userService.getUserDetailsByUserId(userId);
    }
}
