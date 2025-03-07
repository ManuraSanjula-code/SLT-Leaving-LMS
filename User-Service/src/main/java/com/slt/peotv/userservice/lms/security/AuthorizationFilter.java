package com.slt.peotv.userservice.lms.security;

import com.nimbusds.jose.JOSEException;
import com.slt.peotv.userservice.lms.SpringApplicationContext;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.security.jwt.token.converter.TokenConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    public AuthorizationFilter(AuthenticationManager authManager, UserRepository userRepository) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        String header = req.getHeader(SecurityConstants.HEADER_STRING);

        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = null;
        try {
            authentication = getAuthentication(req);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) throws ParseException, JOSEException {

        String authorizationHeader = request.getHeader(SecurityConstants.HEADER_STRING);

        if (authorizationHeader == null) {
            return null;
        }

        String signedToken = authorizationHeader.replace(SecurityConstants.TOKEN_PREFIX, "");
        TokenConverter tokenConverter = (TokenConverter) SpringApplicationContext.getBean("tokenConverter");
        String token = tokenConverter.decryptToken(signedToken);
        String userId = tokenConverter.validateTokenSignature(token, request);

        if(userId != null){
            return new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

        }else {
            return null;
        }

    }

}
