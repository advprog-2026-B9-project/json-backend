package com.b9.json.jsonplatform.auth.infrastructure.controller;

import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Kita mock repository-nya agar tidak butuh database asli
    @MockBean
    private UserRepository userRepository;

    @Test
    void testShowRegisterForm() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("Register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testRegisterUserWithUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("username", "customUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/register?success"));

        // Verifikasi bahwa method save dipanggil 1 kali
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserWithoutUsername() throws Exception {
        // Test skenario ketika username kosong, controller harus memotong string dari email
        mockMvc.perform(post("/auth/register")
                        .param("email", "tanpa_user@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/register?success"));

        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void testListUsers() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        // Simulasi jika findAll() dipanggil, kembalikan list buatan kita
        Mockito.when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/auth/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("UserList"))
                .andExpect(model().attributeExists("users"));
    }
}