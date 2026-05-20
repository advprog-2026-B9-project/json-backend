package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.application.service.KycService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.containsString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private KycService kycService;

    @Test
    void testRegisterUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setUsername("customUser");

        Mockito.when(authService.registerUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("customUser"));

        Mockito.verify(authService, Mockito.times(1)).registerUser(any(User.class));
    }

    @Test
    void testLoginUserSuccess() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");

        Mockito.when(authService.loginUser("test@example.com", "password123")).thenReturn(user);

        User loginData = new User();
        loginData.setEmail("test@example.com");
        loginData.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testLoginUserFailure() throws Exception {
        Mockito.when(authService.loginUser("test@example.com", "wrong")).thenReturn(null);

        User loginData = new User();
        loginData.setEmail("test@example.com");
        loginData.setPassword("wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email atau password salah!"));
    }

    @Test
    void testListUsers_NoFilter_ShouldReturnAll() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        Mockito.when(authService.findAllUsers(null)).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/auth/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testListUsers_FilterBanned_ShouldReturnBannedUsers() throws Exception {
        User bannedUser = new User();
        bannedUser.setUsername("banneduser");

        Mockito.when(authService.findAllUsers("banned")).thenReturn(List.of(bannedUser));

        mockMvc.perform(get("/auth/list")
                        .param("status", "banned")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("banneduser"));
    }

    @Test
    void testListUsers_InvalidStatus_ShouldReturnBadRequest() throws Exception {
        Mockito.when(authService.findAllUsers("unknown"))
                .thenThrow(new IllegalArgumentException("Status tidak valid: unknown"));

        mockMvc.perform(get("/auth/list")
                        .param("status", "unknown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Status tidak valid")));
    }
}