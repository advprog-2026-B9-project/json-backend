package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KycServiceImpl kycService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setEmail("titipers@example.com");
        sampleUser.setPassword("encoded");
        sampleUser.setUsername("titipers01");
        sampleUser.setRole(UserRole.TITIPERS);
        sampleUser.setKycStatus(KycStatus.UNVERIFIED);
    }

    // ── submitKyc ─────────────────────────────────────────────────────────────

    @Test
    void testSubmitKyc_ValidUser_ShouldSetPendingVerification() {
        when(userRepository.findByEmail("titipers@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = kycService.submitKyc(
                "titipers@example.com",
                "Nama Lengkap KTP",
                "3201012345678901",
                "https://storage.example.com/ktp.jpg"
        );

        assertNotNull(result);
        assertEquals(KycStatus.PENDING_VERIFICATION, result.getKycStatus());
        assertEquals("Nama Lengkap KTP", result.getFullName());
        assertEquals("3201012345678901", result.getNikKtp());
        assertEquals("https://storage.example.com/ktp.jpg", result.getKtpImageUrl());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testSubmitKyc_UserNotFound_ShouldReturnNull() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

        User result = kycService.submitKyc(
                "ghost@example.com",
                "Nama",
                "1234567890123456",
                null
        );

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSubmitKyc_ShouldNotChangeRole() {
        when(userRepository.findByEmail("titipers@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = kycService.submitKyc(
                "titipers@example.com",
                "Nama",
                "1234567890123456",
                null
        );

        // Role belum berubah, masih TITIPERS sampai admin approve
        assertEquals(UserRole.TITIPERS, result.getRole());
    }

    // ── findPendingKyc ────────────────────────────────────────────────────────

    @Test
    void testFindPendingKyc_ShouldReturnOnlyPendingUsers() {
        User pendingUser = new User();
        pendingUser.setEmail("pending@example.com");
        pendingUser.setKycStatus(KycStatus.PENDING_VERIFICATION);

        User unverifiedUser = new User();
        unverifiedUser.setEmail("unverified@example.com");
        unverifiedUser.setKycStatus(KycStatus.UNVERIFIED);

        User verifiedUser = new User();
        verifiedUser.setEmail("verified@example.com");
        verifiedUser.setKycStatus(KycStatus.VERIFIED);

        when(userRepository.findAll()).thenReturn(List.of(pendingUser, unverifiedUser, verifiedUser));

        List<User> result = kycService.findPendingKyc();

        assertEquals(1, result.size());
        assertEquals("pending@example.com", result.get(0).getEmail());
    }

    @Test
    void testFindPendingKyc_NoPendingUsers_ShouldReturnEmptyList() {
        User unverifiedUser = new User();
        unverifiedUser.setKycStatus(KycStatus.UNVERIFIED);

        when(userRepository.findAll()).thenReturn(List.of(unverifiedUser));

        List<User> result = kycService.findPendingKyc();

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindPendingKyc_MultiplePendingUsers_ShouldReturnAll() {
        User pending1 = new User();
        pending1.setEmail("pending1@example.com");
        pending1.setKycStatus(KycStatus.PENDING_VERIFICATION);

        User pending2 = new User();
        pending2.setEmail("pending2@example.com");
        pending2.setKycStatus(KycStatus.PENDING_VERIFICATION);

        when(userRepository.findAll()).thenReturn(List.of(pending1, pending2));

        List<User> result = kycService.findPendingKyc();

        assertEquals(2, result.size());
    }

    // ── reviewKyc ─────────────────────────────────────────────────────────────

    @Test
    void testReviewKyc_Approved_ShouldUpgradeToJastiper() {
        sampleUser.setKycStatus(KycStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("titipers@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = kycService.reviewKyc("titipers@example.com", true);

        assertNotNull(result);
        assertEquals(UserRole.JASTIPER, result.getRole());
        assertEquals(KycStatus.VERIFIED, result.getKycStatus());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testReviewKyc_Rejected_ShouldResetToUnverified() {
        sampleUser.setKycStatus(KycStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("titipers@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = kycService.reviewKyc("titipers@example.com", false);

        assertNotNull(result);
        assertEquals(UserRole.TITIPERS, result.getRole()); // role tidak naik
        assertEquals(KycStatus.UNVERIFIED, result.getKycStatus());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void testReviewKyc_StatusNotPending_ShouldReturnNull() {
        sampleUser.setKycStatus(KycStatus.UNVERIFIED); // bukan PENDING
        when(userRepository.findByEmail("titipers@example.com")).thenReturn(sampleUser);

        User result = kycService.reviewKyc("titipers@example.com", true);

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testReviewKyc_AlreadyVerified_ShouldReturnNull() {
        sampleUser.setKycStatus(KycStatus.VERIFIED); // sudah verified, bukan PENDING
        sampleUser.setRole(UserRole.JASTIPER);
        when(userRepository.findByEmail("titipers@example.com")).thenReturn(sampleUser);

        User result = kycService.reviewKyc("titipers@example.com", true);

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testReviewKyc_UserNotFound_ShouldReturnNull() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

        User result = kycService.reviewKyc("ghost@example.com", true);

        assertNull(result);
        verify(userRepository, never()).save(any());
    }
}