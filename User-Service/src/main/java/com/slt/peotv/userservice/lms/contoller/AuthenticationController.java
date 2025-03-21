package com.slt.peotv.userservice.lms.contoller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.slt.peotv.userservice.lms.SpringApplicationContext;
import com.slt.peotv.userservice.lms.contoller.TempAuth.TempAuthenticationProvider;
import com.slt.peotv.userservice.lms.entity.TempUser;
import com.slt.peotv.userservice.lms.repository.TempUserRepo;
import com.slt.peotv.userservice.lms.security.SecurityConstants;
import com.slt.peotv.userservice.lms.security.UserPrincipal;
import com.slt.peotv.userservice.lms.security.jwt.token.creator.TokenCreator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.slt.peotv.userservice.lms.shared.model.request.LoginRequestModel;

import java.security.NoSuchAlgorithmException;

@RestController
public class AuthenticationController {

	private final TempAuthenticationProvider authenticationManager;
	private final TempUserRepo tempUserRepo;
    public AuthenticationController(
            TempAuthenticationProvider authenticationManager, TempUserRepo tempUserRepo
    ) {
        this.authenticationManager = authenticationManager;
        this.tempUserRepo = tempUserRepo;
    }

    @PostMapping("/users/login")
	public void theFakeLogin(@RequestBody LoginRequestModel loginRequestModel)
	{
		throw new IllegalStateException("This method should not be called. This method is implemented by Spring Security");
	}

	@PostMapping("/users/login/temp")
	public ResponseEntity<String> tempLogin(@RequestBody LoginRequestModel loginRequest, HttpServletResponse res)
	{
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getEmail() + " " + "TEMP",
						loginRequest.getPassword()
				)
		);
		String userName = ((UserPrincipal) authentication.getPrincipal()).getUsername();
		TokenCreator tokenCreator = (TokenCreator) SpringApplicationContext.getBean("tokenCreator");
		TempUser tempUser = tempUserRepo.findByEmail(userName);

		SignedJWT signedJWT = null;
		try {
			signedJWT = tokenCreator.createSignedJWT(authentication, tempUser);
		} catch (NoSuchAlgorithmException | JOSEException e) {
			throw new RuntimeException(e);
		}

		String encryptToken = null;
		try {
			encryptToken = tokenCreator.encryptToken(signedJWT);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}

		ResponseCookie jwtCookie = ResponseCookie.from("jwt", encryptToken)
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(7 * 24 * 60 * 60)
				.sameSite("None")
				.build();

		ResponseCookie userIdCookie = ResponseCookie.from("userId", tempUser.getUserId())
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(7 * 24 * 60 * 60)
				.sameSite("None")
				.build();


		res.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
		res.addHeader(HttpHeaders.SET_COOKIE, userIdCookie.toString());

		res.addHeader(SecurityConstants.HEADER_STRING , SecurityConstants.TOKEN_PREFIX + encryptToken);
		res.addHeader("UserID" , tempUser.getUserId());

		res.addHeader("ROLE", "ROLE_TEMP");

		return ResponseEntity.ok("");
	}
}
