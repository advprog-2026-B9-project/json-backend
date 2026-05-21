package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceClientImplTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthServiceClientImpl authServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindEmailById_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("jastiper@gmail.com");

        when(authService.findById(userId)).thenReturn(user);

        String result = authServiceClient.findEmailById(userId);

        assertEquals("jastiper@gmail.com", result);
    }

    @Test
    void testFindEmailById_UserNotFound_ShouldThrow() {
        UUID userId = UUID.randomUUID();
        when(authService.findById(userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                authServiceClient.findEmailById(userId)
        );
    }

    @Test
    void testFindUserIdByUsername_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("jastiper_budi");

        when(authService.findByUsername("jastiper_budi")).thenReturn(user);

        UUID result = authServiceClient.findUserIdByUsername("jastiper_budi");

        assertEquals(userId, result);
    }

    @Test
    void testFindUserIdByUsername_NotFound_ShouldThrow() {
        when(authService.findByUsername("ghost")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                authServiceClient.findUserIdByUsername("ghost")
        );
    }

    @Test
    void testAddRating_ShouldDelegate() {
        authServiceClient.addRating("jastiper@gmail.com", 5);

        verify(authService).addRating("jastiper@gmail.com", 5);
    }
}