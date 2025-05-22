package com.slt.peotv.userservice.lms.Login;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class TempLoginTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private static String USER_ID = "";
	private static String JWT_TOKEN = "";

	@Test
	void login() throws Exception {
		// Prepare login request payload
		Map<String, String> loginRequest = new HashMap<>();
		loginRequest.put("email", "john.doe@example.com");
		loginRequest.put("password", "securepassword");

		String requestBody = objectMapper.writeValueAsString(loginRequest);

		// Perform the login request
		MvcResult result = mockMvc.perform(post("/users/login/temp")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
						.with(csrf())) // Add CSRF token for Spring Security
				.andExpect(status().isOk())
				.andExpect(header().exists("Authorization"))
				.andExpect(header().exists("UserID"))
				.andReturn();

		// Extract headers from response
		JWT_TOKEN = result.getResponse().getHeader("Authorization");
		USER_ID = result.getResponse().getHeader("UserID");

		// Assertions to verify headers are not null/empty
		assertNotNull(JWT_TOKEN, "Authorization header should not be null");
		assertNotNull(USER_ID, "UserID header should not be null");
		assertFalse(JWT_TOKEN.isEmpty(), "Authorization header should not be empty");
		assertFalse(USER_ID.isEmpty(), "UserID header should not be empty");

		// Print the extracted values for verification
		System.out.println("Extracted JWT Token: " + JWT_TOKEN);
		System.out.println("Extracted User ID: " + USER_ID);

		// Optional: Additional assertions based on your expected format
		// assertTrue(JWT_TOKEN.startsWith("Bearer "), "JWT token should start with 'Bearer '");
	}

	@Test
	void loginWithInvalidCredentials() throws Exception {
		// Test case for invalid credentials
		Map<String, String> loginRequest = new HashMap<>();
		loginRequest.put("email", "invalid@example.com");
		loginRequest.put("password", "wrongpassword");

		String requestBody = objectMapper.writeValueAsString(loginRequest);

		mockMvc.perform(post("/users/login/temp")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginWithMissingFields() throws Exception {
		// Test case for missing required fields
		Map<String, String> loginRequest = new HashMap<>();
		loginRequest.put("email", "john.doe@example.com");
		// Missing password field

		String requestBody = objectMapper.writeValueAsString(loginRequest);

		mockMvc.perform(post("/users/login/temp")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
						.with(csrf())) // Add CSRF token
				.andExpect(status().isBadRequest());
	}

	// Getter methods to access the extracted values from other test methods
	public static String getUserId() {
		return USER_ID;
	}

	public static String getJwtToken() {
		return JWT_TOKEN;
	}
}