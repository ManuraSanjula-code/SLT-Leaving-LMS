package com.slt.peotv.lmsmangmentservice.security;

import com.nimbusds.jose.JOSEException;
import com.slt.peotv.lmsmangmentservice.SpringApplicationContext;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    public AuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        // First try to get the token from Authorization header
        String authorizationHeader = req.getHeader(SecurityConstants.HEADER_STRING);
        String token = null;

        // Check if the Authorization header exists and has the correct prefix
        if (authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            token = authorizationHeader.replace(SecurityConstants.TOKEN_PREFIX, "");
        } else {
            // If no Authorization header, try to get token from cookie
            token = extractJwtTokenFromCookie(req);
            if (token == null) {
                // No token found in either header or cookie
                chain.doFilter(req, res);
                return;
            }
        }

        // We have a token now, authenticate
        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(req, token, authorizationHeader);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }

        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, String signedToken, String originalAuthHeader) throws ParseException, JOSEException {
        if (signedToken == null) {
            return null;
        }

        TokenConverter tokenConverter = (TokenConverter) SpringApplicationContext.getBean("tokenConverter");

        // Decrypt token to validate it locally
        String decryptedToken = tokenConverter.decryptToken(signedToken);

        // Pass the original token in the request to the user service (not the decrypted one)
        UserRest user = tokenConverter.validateTokenSignature(decryptedToken, request, originalAuthHeader != null ? originalAuthHeader : SecurityConstants.TOKEN_PREFIX + signedToken);

        if(user != null){
            return new UsernamePasswordAuthenticationToken(new UserPrincipal(user), null, user.getAuthorities());
        } else {
            return null;
        }
    }

    private String extractJwtTokenFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}