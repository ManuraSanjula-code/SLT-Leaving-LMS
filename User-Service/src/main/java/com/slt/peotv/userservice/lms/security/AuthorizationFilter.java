package com.slt.peotv.userservice.lms.security;

import com.nimbusds.jose.JOSEException;
import com.slt.peotv.userservice.lms.SpringApplicationContext;
import com.slt.peotv.userservice.lms.security.jwt.token.converter.TokenConverter;
import com.slt.peotv.userservice.lms.service.UserService;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.text.ParseException;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);
    private final UserService userService;
    private final Object tokenConverterLock = new Object(); // Lock for tokenConverter access

    public AuthorizationFilter(AuthenticationManager authManager, UserService userService) {
        super(authManager);
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ParseException | JOSEException e) {
            logger.error("Authentication failed due to token processing error", e);
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            logger.error("Unexpected authentication error", e);
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
            throws ParseException, JOSEException {
        String tokenToProcess = extractTokenFromRequest(request);
        if (tokenToProcess == null) {
            return null;
        }
        TokenConverter tokenConverter = getTokenConverter();
        String decryptedToken = decryptToken(tokenConverter, tokenToProcess);
        if (decryptedToken == null) {
            return null;
        }

        return createAuthenticationToken(tokenConverter, decryptedToken, request);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(SecurityConstants.HEADER_STRING);
        String jwtToken = extractJwtTokenFromCookie(request);

        if (jwtToken != null) {
            return jwtToken;
        }else if (authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return authorizationHeader.replace(SecurityConstants.TOKEN_PREFIX, "");
        }
        return null;
    }

    private TokenConverter getTokenConverter() {
        synchronized (tokenConverterLock) {
            return (TokenConverter) SpringApplicationContext.getBean("tokenConverter");
        }
    }

    private String decryptToken(TokenConverter tokenConverter, String token) {
        try {
            return tokenConverter.decryptToken(token);
        } catch (Exception e) {
            logger.error("Token decryption failed", e);
            return null;
        }
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(
            TokenConverter tokenConverter, String decryptedToken, HttpServletRequest request)
            throws JOSEException, ParseException {

        UserPrincipal userPrincipal = (UserPrincipal) tokenConverter.validateTokenSignature(userService, decryptedToken, request);
        if (userPrincipal == null) {
            logger.warn("Token validation failed - no user principal returned");
            return null;
        }
        logger.debug("Authenticating user: {}", userPrincipal.getUserEntity().getUserId());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                userPrincipal.getUserEntity().getEncryptedPassword(),
                userPrincipal.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
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