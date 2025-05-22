package com.slt.peotv.userservice.lms.Authority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slt.peotv.userservice.lms.shared.model.request.AuthReq;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorityTestForUpdate {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String USER_ID = "";
    private static String JWT_TOKEN = "";

    @Test
    @Order(1)
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
    }

    @Test
    @Order(2)
    void testCreateAuthority() throws Exception {
        // Ensure login was successful first
        assertNotNull(JWT_TOKEN, "JWT Token should be available from login test");
        assertNotNull(USER_ID, "User ID should be available from login test");
        assertFalse(JWT_TOKEN.isEmpty(), "JWT Token should not be empty");
        assertFalse(USER_ID.isEmpty(), "User ID should not be empty");

        // Prepare AuthReq payload
        AuthReq authReq = new AuthReq();
        authReq.setNewName("UPDATE_AUTHORITY");
        authReq.setWeight(100);

        String requestBody = objectMapper.writeValueAsString(authReq);

        // Build the URL with actual user ID
        String authUrl = "/users/auth/" + USER_ID;

        System.out.println("Testing authority endpoint: " + authUrl);
        System.out.println("Using JWT Token: " + JWT_TOKEN);
        System.out.println("Request Body: " + requestBody);

        // Perform the authority request with JWT token
        MvcResult result = mockMvc.perform(post(authUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", JWT_TOKEN) // Add JWT token as header
                        .content(requestBody)
                        .with(csrf())) // Add CSRF token for Spring Security
                .andExpect(status().isOk()) // Expecting 200 OK, adjust based on your endpoint
                .andReturn();

        // Print response for verification
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Authority Response: " + responseBody);
        System.out.println("Authority Response Status: " + result.getResponse().getStatus());

        // Additional assertions based on your expected response
        assertNotNull(responseBody, "Response body should not be null");
    }

    @Test
    @Order(3)
    void testCreateAuthorityWithoutJWT() throws Exception {
        // Test authority endpoint without JWT token (should fail)
        AuthReq authReq = new AuthReq();
        authReq.setNewName("UPDATE_AUTHORITY");
        authReq.setWeight(100);

        String requestBody = objectMapper.writeValueAsString(authReq);
        String authUrl = "/users/auth/" + (USER_ID.isEmpty() ? "testUserId" : USER_ID);

        // Perform the authority request without JWT token (should be unauthorized)
        mockMvc.perform(post(authUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Expecting 401 Unauthorized
    }

    @Test
    @Order(4)
    void testCreateAuthorityWithInvalidJWT() throws Exception {
        // Test authority endpoint with invalid JWT token
        AuthReq authReq = new AuthReq();
        authReq.setNewName("UPDATE_AUTHORITY");
        authReq.setWeight(100);

        String requestBody = objectMapper.writeValueAsString(authReq);
        String authUrl = "/users/auth/" + (USER_ID.isEmpty() ? "testUserId" : USER_ID);

        // Perform the authority request with invalid JWT token
        mockMvc.perform(post(authUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Expecting 401 Unauthorized
    }

    /*@Test
    @Order(5)
    void testCreateAuthorityWithMissingFields() throws Exception {
        // Ensure we have valid credentials
        assertNotNull(JWT_TOKEN, "JWT Token should be available");
        assertNotNull(USER_ID, "User ID should be available");

        // Test with missing required fields
        AuthReq authReq = new AuthReq();
        authReq.setNewName("UPDATE_AUTHORITY");
        // Missing oldName and weight

        String requestBody = objectMapper.writeValueAsString(authReq);
        String authUrl = "/users/auth/" + USER_ID;

        // Perform the authority request with incomplete data
        mockMvc.perform(post(authUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", JWT_TOKEN)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Expecting 400 Bad Request
    }*/

    // Getter methods to access the extracted values from other test classes
    public static String getUserId() {
        return USER_ID;
    }

    public static String getJwtToken() {
        return JWT_TOKEN;
    }
}