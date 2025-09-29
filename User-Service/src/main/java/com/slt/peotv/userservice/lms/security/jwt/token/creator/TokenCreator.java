package com.slt.peotv.userservice.lms.security.jwt.token.creator;

import static java.util.stream.Collectors.toList;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.slt.peotv.userservice.lms.entity.TempUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.slt.peotv.userservice.lms.security.SecurityConstants;
import com.slt.peotv.userservice.lms.security.UserPrincipal;
import com.slt.peotv.userservice.lms.security.jwt.property.JwtConfiguration;

@Service
public class TokenCreator {
    @Autowired
    private JwtConfiguration jwtConfiguration;


    public SignedJWT createSignedJWT(Authentication auth) throws NoSuchAlgorithmException, JOSEException {
        UserPrincipal applicationUser = (UserPrincipal) auth.getPrincipal();
        JWTClaimsSet jwtClaimSet = createJWTClaimSet_(auth, applicationUser);
        return createAndSignJWT(jwtClaimSet);
    }


    public SignedJWT createSignedJWTForTemp(Authentication auth, TempUser tempUser) throws NoSuchAlgorithmException, JOSEException {
        UserPrincipal applicationUser = (UserPrincipal) auth.getPrincipal();
        JWTClaimsSet jwtClaimSet = createJWTClaimSetForTemp(auth, applicationUser, tempUser);
        return createAndSignJWT(jwtClaimSet);
    }


    public SignedJWT createSignedJWTForLMS(String email) throws NoSuchAlgorithmException, JOSEException {
        JWTClaimsSet jwtClaimSet = createJWTClaimSetForLMS(email);
        return createAndSignJWT(jwtClaimSet);
    }


    public SignedJWT createSignedJWT(String email) throws NoSuchAlgorithmException, JOSEException {
        JWTClaimsSet jwtClaimSet = createJWTClaimSet(email);
        return createAndSignJWT(jwtClaimSet);
    }


    private SignedJWT createAndSignJWT(JWTClaimsSet jwtClaimSet) throws NoSuchAlgorithmException, JOSEException {
        KeyPair rsaKeys = generateKeyPair();
        JWK jwk = new RSAKey.Builder((RSAPublicKey) rsaKeys.getPublic())
                .keyID(UUID.randomUUID().toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .jwk(jwk)
                        .type(JOSEObjectType.JWT)
                        .build(),
                jwtClaimSet);

        RSASSASigner signer = new RSASSASigner(rsaKeys.getPrivate());
        signedJWT.sign(signer);
        return signedJWT;
    }


    private JWTClaimsSet createJWTClaimSet(String email) {
        return new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("SLT PEO TV")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + SecurityConstants.PASSWORD_RESET_EXPIRATION_TIME))
                .build();
    }


    private JWTClaimsSet createJWTClaimSetForLMS(String email) {
        return new JWTClaimsSet.Builder()
                .subject(email + " LMS")
                .issuer("SLT PEO TV")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + SecurityConstants.PASSWORD_RESET_EXPIRATION_TIME))
                .build();
    }


    private JWTClaimsSet createJWTClaimSet(Authentication auth, UserPrincipal applicationUser) {
        return new JWTClaimsSet.Builder()
                .subject(applicationUser.getUserId())
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(toList()))
                .issuer("SLT PEO TV")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .build();
    }


    private JWTClaimsSet createJWTClaimSet_(Authentication auth, UserPrincipal applicationUser) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(applicationUser.getUserId())
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issuer("SLT PEO TV")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME));

        // Add additional claims if available
        if (applicationUser.getHighestRolePriority() != null) {
            builder.claim("highestPriority", applicationUser.getHighestRolePriority());
        }

        if (applicationUser.getAuthorityWeights() != null) {
            builder.claim("authorityWeights", applicationUser.getAuthorityWeights());
        }

        return builder.build();
    }


    private JWTClaimsSet createJWTClaimSet(Authentication auth, UserPrincipal applicationUser, TempUser tempUser) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(applicationUser.getUserId() + " TEMP")
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(toList()))
                .issuer("SLT PEO TV")
                .issueTime(new Date());

        // Use the correct method name from TempUser entity
        Date expirationTime;
        if (tempUser.getExpireTime() != null) {
            expirationTime = tempUser.getExpireTime();
        } else {
            // Fallback to standard expiration time if expireTime is null
            expirationTime = new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME);
        }

        builder.expirationTime(expirationTime);
        return builder.build();
    }

    private JWTClaimsSet createJWTClaimSetForTemp(Authentication auth, UserPrincipal applicationUser, TempUser tempUser) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(applicationUser.getUserId() + " TEMP")
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issuer("SLT PEO TV")
                .issueTime(new Date());

        // Handle expiration time safely using the correct method name
        Date expirationTime;
        if (tempUser.getExpireTime() != null) {
            expirationTime = tempUser.getExpireTime();
        } else {
            expirationTime = new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME);
        }

        builder.expirationTime(expirationTime);

        // Add additional claims from UserPrincipal if available
        if (applicationUser.getClaims() != null) {
            applicationUser.getClaims().forEach(builder::claim);
        }

        return builder.build();
    }


    private JWTClaimsSet createJWTClaimSet(Authentication auth, UserPrincipal applicationUser, List<String> roles) {
        return new JWTClaimsSet.Builder()
                .subject(applicationUser.getUsername())
                .claim("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(toList()))
                .claim("customRoles", roles)
                .issuer("SLT PEO TV")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .build();
    }


    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.genKeyPair();
    }


    public String encryptToken(SignedJWT signedJWT) throws JOSEException {
        try {
            DirectEncrypter directEncrypter = new DirectEncrypter(jwtConfiguration.getPrivateKey().getBytes());

            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256)
                            .contentType("JWT")
                            .build(),
                    new Payload(signedJWT));

            jweObject.encrypt(directEncrypter);
            return jweObject.serialize();
        } catch (Exception e) {
            throw new JOSEException("Failed to encrypt token", e);
        }
    }
}