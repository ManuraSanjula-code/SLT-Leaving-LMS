package com.slt.peotv.userservice.lms.security;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.nimbusds.jose.JOSEException;
import com.slt.peotv.userservice.lms.SpringApplicationContext;
import com.slt.peotv.userservice.lms.repository.UserRepository;
import com.slt.peotv.userservice.lms.security.jwt.token.converter.TokenConverter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends BasicAuthenticationFilter {

	public AuthorizationFilter(AuthenticationManager authManager, UserRepository userRepository) {
		super(authManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

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

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
			throws ParseException, JOSEException {
		String authorizationHeader = request.getHeader(SecurityConstants.HEADER_STRING);
		String jwtToken = extractJwtTokenFromCookie(request);

		if (authorizationHeader == null && jwtToken == null) {
			return null;
		}

		String tokenToProcess = null;
		if (authorizationHeader != null && authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			tokenToProcess = authorizationHeader.replace(SecurityConstants.TOKEN_PREFIX, "");
		} else if (jwtToken != null) {
			tokenToProcess = jwtToken;
		}

		if (tokenToProcess == null) {
			return null;
		}

		TokenConverter tokenConverter = (TokenConverter) SpringApplicationContext.getBean("tokenConverter");
		String decryptedToken = null;
		try {
			decryptedToken = tokenConverter.decryptToken(tokenToProcess);
		} catch (Exception e) {
			System.err.println("Error decrypting token: " + e.getMessage());
			return null;
		}

		if (decryptedToken == null) {
			return null;
		}

		String userId = null;
		try {
			userId = tokenConverter.validateTokenSignature(decryptedToken, request);
		} catch (Exception e) {
			System.err.println("Error validating token signature: " + e.getMessage());
			return null;
		}

		if (userId != null) {
			return new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
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
