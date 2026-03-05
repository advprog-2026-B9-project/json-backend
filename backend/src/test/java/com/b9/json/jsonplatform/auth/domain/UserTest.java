package com.b9.json.jsonplatform.auth.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserGettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setEmail("mahasiswa@ui.ac.id");
        user.setPassword("rahasia123");
        user.setUsername("mahasiswa_ui");

        assertEquals(1L, user.getId());
        assertEquals("mahasiswa@ui.ac.id", user.getEmail());
        assertEquals("rahasia123", user.getPassword());
        assertEquals("mahasiswa_ui", user.getUsername());

        // Cek default value
        assertEquals("TITIPERS", user.getRole());
    }

    @Test
    void testUserCustomRole() {
        User user = new User();
        user.setRole("ADMIN");
        assertEquals("ADMIN", user.getRole());
    }
}