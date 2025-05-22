package com.slt.peotv.userservice.lms.Section;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slt.peotv.userservice.lms.shared.model.request.SectionReq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SectionTest2 {

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
    void testCreateSection() throws Exception {
        // Ensure login was successful first
        assertNotNull(JWT_TOKEN, "JWT Token should be available from login test");
        assertNotNull(USER_ID, "User ID should be available from login test");
        assertFalse(JWT_TOKEN.isEmpty(), "JWT Token should not be empty");
        assertFalse(USER_ID.isEmpty(), "User ID should not be empty");

        // Prepare SectionReq payload
        SectionReq sectionReq = new SectionReq();
        sectionReq.setPublicId("SEC001");
        sectionReq.setSection("Development Team");

        // Sample users to add and delete
        List<String> addedUsers = Arrays.asList("user123", "user456", "user789");
        List<String> deletedUsers = Arrays.asList("user111", "user222");

        sectionReq.setAddedUsers(addedUsers);
        sectionReq.setDeletedUsers(deletedUsers);

        String requestBody = objectMapper.writeValueAsString(sectionReq);

        // Build the URL with actual user ID
        String sectionUrl = "/users/section/" + USER_ID;

        System.out.println("Testing section endpoint: " + sectionUrl);
        System.out.println("Using JWT Token: " + JWT_TOKEN);
        System.out.println("Request Body: " + requestBody);

        // Perform the section request with JWT token
        MvcResult result = mockMvc.perform(post(sectionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", JWT_TOKEN) // Add JWT token as header
                        .content(requestBody)
                        .with(csrf())) // Add CSRF token for Spring Security
                .andExpect(status().isOk()) // Expecting 200 OK, adjust based on your endpoint
                .andReturn();

        // Print response for verification
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Section Response: " + responseBody);
        System.out.println("Section Response Status: " + result.getResponse().getStatus());

        // Additional assertions based on your expected response
        assertNotNull(responseBody, "Response body should not be null");
    }

    @Test
    @Order(3)
    void testCreateSectionWithEmptyLists() throws Exception {
        // Ensure we have valid credentials
        assertNotNull(JWT_TOKEN, "JWT Token should be available");
        assertNotNull(USER_ID, "User ID should be available");

        // Prepare SectionReq with empty user lists
        SectionReq sectionReq = new SectionReq();
        sectionReq.setPublicId("SEC006");
        sectionReq.setSection("HR Team");
        sectionReq.setAddedUsers(Arrays.asList()); // Empty list
        sectionReq.setDeletedUsers(Arrays.asList()); // Empty list

        String requestBody = objectMapper.writeValueAsString(sectionReq);
        String sectionUrl = "/users/section/" + USER_ID;

        // Perform the section request with empty lists
        MvcResult result = mockMvc.perform(post(sectionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", JWT_TOKEN)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()) // Adjust expected status
                .andReturn();

        System.out.println("Section with empty lists response: " + result.getResponse().getContentAsString());
    }

    @Test
    @Order(4)
    void testCreateSectionWithoutJWT() throws Exception {
        // Test section endpoint without JWT token (should fail)
        SectionReq sectionReq = new SectionReq();
        sectionReq.setPublicId("SEC003");
        sectionReq.setSection("Admin Team");
        sectionReq.setAddedUsers(Arrays.asList("admin1", "admin2"));
        sectionReq.setDeletedUsers(Arrays.asList());

        String requestBody = objectMapper.writeValueAsString(sectionReq);
        String sectionUrl = "/users/section/" + (USER_ID.isEmpty() ? "testUserId" : USER_ID);

        // Perform the section request without JWT token (should be unauthorized)
        mockMvc.perform(post(sectionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Expecting 401 Unauthorized
    }

    @Test
    @Order(5)
    void testCreateSectionWithInvalidJWT() throws Exception {
        // Test section endpoint with invalid JWT token
        SectionReq sectionReq = new SectionReq();
        sectionReq.setPublicId("SEC004");
        sectionReq.setSection("QA Team");
        sectionReq.setAddedUsers(Arrays.asList("qa1", "qa2"));
        sectionReq.setDeletedUsers(Arrays.asList());

        String requestBody = objectMapper.writeValueAsString(sectionReq);
        String sectionUrl = "/users/section/" + (USER_ID.isEmpty() ? "testUserId" : USER_ID);

        // Perform the section request with invalid JWT token
        mockMvc.perform(post(sectionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Expecting 401 Unauthorized
    }

    @Test
    @Order(6)
    void testCreateSectionWithMissingFields() throws Exception {
        // Ensure we have valid credentials
        assertNotNull(JWT_TOKEN, "JWT Token should be available");
        assertNotNull(USER_ID, "User ID should be available");

        // Test with missing required fields
        SectionReq sectionReq = new SectionReq();
        sectionReq.setPublicId("SEC005");
        // Missing section name and user lists

        String requestBody = objectMapper.writeValueAsString(sectionReq);
        String sectionUrl = "/users/section/" + USER_ID;

        // Perform the section request with incomplete data
        mockMvc.perform(post(sectionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", JWT_TOKEN)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // Expecting 400 Bad Request
    }

    @Test
    @Order(7)
    void testCreateSectionWithNullValues() throws Exception {
        // Ensure we have valid credentials
        assertNotNull(JWT_TOKEN, "JWT Token should be available");
        assertNotNull(USER_ID, "User ID should be available");

        // Test with null values
        SectionReq sectionReq = new SectionReq();
        sectionReq.setPublicId(null);
        sectionReq.setSection(null);
        sectionReq.setAddedUsers(null);
        sectionReq.setDeletedUsers(null);

        String requestBody = objectMapper.writeValueAsString(sectionReq);
        String sectionUrl = "/users/section/" + USER_ID;

        // Perform the section request with null values
        mockMvc.perform(post(sectionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", JWT_TOKEN)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // Expecting 400 Bad Request
    }

    @Test
    @Order(8)
    void testCreateSectionWithLargeUserLists() throws Exception {
        // Ensure we have valid credentials
        assertNotNull(JWT_TOKEN, "JWT Token should be available");
        assertNotNull(USER_ID, "User ID should be available");

        // Test with large user lists
        SectionReq sectionReq = new SectionReq();
        sectionReq.setPublicId("SEC006");
        sectionReq.setSection("Large Team");

        // Create large lists of users
        List<String> largeAddedUsers = Arrays.asList(
                "user001", "user002", "user003", "user004", "user005",
                "user006", "user007", "user008", "user009", "user010"
        );
        List<String> largeDeletedUsers = Arrays.asList(
                "olduser001", "olduser002", "olduser003", "olduser004", "olduser005"
        );

        sectionReq.setAddedUsers(largeAddedUsers);
        sectionReq.setDeletedUsers(largeDeletedUsers);

        String requestBody = objectMapper.writeValueAsString(sectionReq);
        String sectionUrl = "/users/section/" + USER_ID;

        System.out.println("Testing large user lists - Added: " + largeAddedUsers.size() +
                ", Deleted: " + largeDeletedUsers.size());

        // Perform the section request with large user lists
        MvcResult result = mockMvc.perform(post(sectionUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", JWT_TOKEN)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()) // Adjust expected status
                .andReturn();

        System.out.println("Large user lists response: " + result.getResponse().getContentAsString());
    }

    // Getter methods to access the extracted values from other test classes
    public static String getUserId() {
        return USER_ID;
    }

    public static String getJwtToken() {
        return JWT_TOKEN;
    }
}