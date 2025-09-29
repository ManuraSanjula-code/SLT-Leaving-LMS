package com.slt.peotv.lmsmangmentservice.security;

import com.nimbusds.jose.JOSEException;
import com.slt.peotv.lmsmangmentservice.SpringApplicationContext;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.text.ParseException;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    public AuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        String authorizationHeader = req.getHeader(SecurityConstants.HEADER_STRING);
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            token = authorizationHeader.replace(SecurityConstants.TOKEN_PREFIX, "");
        } else {
            token = extractJwtTokenFromCookie(req);
            if (token == null) {
                chain.doFilter(req, res);
                return;
            }
        }

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

        String decryptedToken = tokenConverter.decryptToken(signedToken);

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