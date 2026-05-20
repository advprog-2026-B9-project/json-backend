package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.KycStatus;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.domain.UserRole;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.TransactionRepository;
import com.b9.json.jsonplatform.wallet.domain.TransactionStatus;
import com.b9.json.jsonplatform.wallet.domain.TransactionType;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = new User();
        sampleUser.setId(userId);
        sampleUser.setEmail("test@example.com");
        sampleUser.setPassword("plainpassword");
        sampleUser.setUsername("testuser");
        sampleUser.setRole(UserRole.TITIPERS);
        sampleUser.setKycStatus(KycStatus.UNVERIFIED);
    }

    // ── registerUser ──────────────────────────────────────────────────────────

    @Test
    void testRegisterUser_WithUsername_ShouldUseProvidedUsername() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletService.createWallet(any())).thenReturn(new Wallet(userId));

        User result = authService.registerUser(sampleUser);

        assertEquals("testuser", result.getUsername());
        verify(walletService, times(1)).createWallet(any());
    }

    @Test
    void testRegisterUser_WithoutUsername_ShouldUseEmailPrefix() {
        sampleUser.setUsername(null);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletService.createWallet(any())).thenReturn(new Wallet(userId));

        User result = authService.registerUser(sampleUser);

        assertEquals("test", result.getUsername()); // prefix dari "test@example.com"
        verify(walletService, times(1)).createWallet(any());
    }

    @Test
    void testRegisterUser_WithBlankUsername_ShouldUseEmailPrefix() {
        sampleUser.setUsername("   ");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletService.createWallet(any())).thenReturn(new Wallet(userId));

        User result = authService.registerUser(sampleUser);

        assertEquals("test", result.getUsername());
    }

    @Test
    void testRegisterUser_PasswordShouldBeEncoded() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletService.createWallet(any())).thenReturn(new Wallet(userId));

        User result = authService.registerUser(sampleUser);

        assertNotEquals("plainpassword", result.getPassword());
        assertTrue(result.getPassword().startsWith("$2a$")); // BCrypt prefix
    }

    @Test
    void testRegisterUser_WalletShouldBeCreated() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletService.createWallet(any())).thenReturn(new Wallet(userId));

        authService.registerUser(sampleUser);

        verify(walletService, times(1)).createWallet(any());
    }

    @Test
    void testRegisterUser_DuplicateEmail_ShouldThrowException() {
        when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(sampleUser);

        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(sampleUser);
        });

        verify(userRepository, never()).save(any());
        verify(walletService, never()).createWallet(any());
    }

    // ── loginUser ─────────────────────────────────────────────────────────────

    @Test
    void testLoginUser_ValidCredentials_ShouldReturnUser() {
        // Encode password dulu supaya matches
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        sampleUser.setPassword(encoder.encode("plainpassword"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);

        User result = authService.loginUser("test@example.com", "plainpassword");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testLoginUser_WrongPassword_ShouldReturnNull() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        sampleUser.setPassword(encoder.encode("correctpassword"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);

        User result = authService.loginUser("test@example.com", "wrongpassword");

        assertNull(result);
    }

    @Test
    void testLoginUser_EmailNotFound_ShouldReturnNull() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(null);

        User result = authService.loginUser("notfound@example.com", "anypassword");

        assertNull(result);
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void testUpdateProfile_ExistingUser_ShouldUpdateFields() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updatedData = new User();
        updatedData.setFullName("Full Name Baru");
        updatedData.setUsername("newusername");
        updatedData.setPhoneNumber("08111222333");
        updatedData.setAddress("Jl. Baru No. 1");

        User result = authService.updateProfile("test@example.com", updatedData);

        assertNotNull(result);
        assertEquals("Full Name Baru", result.getFullName());
        assertEquals("newusername", result.getUsername());
        assertEquals("08111222333", result.getPhoneNumber());
        assertEquals("Jl. Baru No. 1", result.getAddress());
    }

    @Test
    void testUpdateProfile_UserNotFound_ShouldReturnNull() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

        User result = authService.updateProfile("ghost@example.com", new User());

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    // ── findByEmail / findByUsername ──────────────────────────────────────────

    @Test
    void testFindByEmail_ShouldReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);

        User result = authService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testFindByUsername_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(sampleUser);

        User result = authService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    // ── findAllUsers ──────────────────────────────────────────────────────────

    @Test
    void testFindAllUsers_NoFilter_ShouldReturnAll() {
        User user1 = new User();
        User user2 = new User();
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = authService.findAllUsers(null);

        assertEquals(2, result.size());
    }

    @Test
    void testFindAllUsers_FilterActive_ShouldReturnOnlyActiveUsers() {
        User activeUser = new User();
        activeUser.setEmail("active@example.com");
        activeUser.setBanned(false);
        activeUser.setKycStatus(KycStatus.UNVERIFIED);

        User bannedUser = new User();
        bannedUser.setBanned(true);
        bannedUser.setKycStatus(KycStatus.UNVERIFIED);

        User pendingUser = new User();
        pendingUser.setBanned(false);
        pendingUser.setKycStatus(KycStatus.PENDING_VERIFICATION);

        when(userRepository.findAll()).thenReturn(List.of(activeUser, bannedUser, pendingUser));

        List<User> result = authService.findAllUsers("active");

        assertEquals(1, result.size());
        assertEquals("active@example.com", result.getFirst().getEmail());
    }

    @Test
    void testFindAllUsers_FilterBanned_ShouldReturnOnlyBannedUsers() {
        User activeUser = new User();
        activeUser.setBanned(false);
        activeUser.setKycStatus(KycStatus.UNVERIFIED);

        User bannedUser = new User();
        bannedUser.setEmail("banned@example.com");
        bannedUser.setBanned(true);
        bannedUser.setKycStatus(KycStatus.UNVERIFIED);

        when(userRepository.findAll()).thenReturn(List.of(activeUser, bannedUser));

        List<User> result = authService.findAllUsers("banned");

        assertEquals(1, result.size());
        assertEquals("banned@example.com", result.getFirst().getEmail());
    }

    @Test
    void testFindAllUsers_FilterPending_ShouldReturnOnlyPendingUsers() {
        User activeUser = new User();
        activeUser.setBanned(false);
        activeUser.setKycStatus(KycStatus.UNVERIFIED);

        User pendingUser = new User();
        pendingUser.setEmail("pending@example.com");
        pendingUser.setBanned(false);
        pendingUser.setKycStatus(KycStatus.PENDING_VERIFICATION);

        when(userRepository.findAll()).thenReturn(List.of(activeUser, pendingUser));

        List<User> result = authService.findAllUsers("pending");

        assertEquals(1, result.size());
        assertEquals("pending@example.com", result.getFirst().getEmail());
    }

    @Test
    void testFindAllUsers_FilterCaseInsensitive_ShouldWork() {
        User bannedUser = new User();
        bannedUser.setBanned(true);
        bannedUser.setKycStatus(KycStatus.UNVERIFIED);

        when(userRepository.findAll()).thenReturn(List.of(bannedUser));

        assertDoesNotThrow(() -> authService.findAllUsers("BANNED"));
        assertDoesNotThrow(() -> authService.findAllUsers("Banned"));
    }

    @Test
    void testFindAllUsers_InvalidStatus_ShouldThrowException() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> authService.findAllUsers("unknown"));
    }

    // ── demoteJastiper ────────────────────────────────────────────────────────

    @Test
    void testDemoteJastiper_ValidJastiper_ShouldDemote() {
        sampleUser.setRole(UserRole.JASTIPER);
        sampleUser.setKycStatus(KycStatus.VERIFIED);
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.demoteJastiper("test@example.com");

        assertNotNull(result);
        assertEquals(UserRole.TITIPERS, result.getRole());
        assertEquals(KycStatus.UNVERIFIED, result.getKycStatus());
    }

    @Test
    void testDemoteJastiper_NotJastiper_ShouldReturnNull() {
        sampleUser.setRole(UserRole.TITIPERS);
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);

        User result = authService.demoteJastiper("test@example.com");

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDemoteJastiper_UserNotFound_ShouldReturnNull() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

        User result = authService.demoteJastiper("ghost@example.com");

        assertNull(result);
    }

    // ── banUser ───────────────────────────────────────────────────────────────

    @Test
    void testBanUser_ExistingUser_ShouldBan() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.banUser("test@example.com");

        assertNotNull(result);
        assertTrue(result.isBanned());
    }

    @Test
    void testBanUser_UserNotFound_ShouldReturnNull() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

        User result = authService.banUser("ghost@example.com");

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    // ── countSuccessfulTransactions ───────────────────────────────────────────

    @Test
    void testCountSuccessfulTransactions_UserNotFound_ShouldReturnZero() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

        long count = authService.countSuccessfulTransactions("ghost@example.com");

        assertEquals(0, count);
    }

    @Test
    void testCountSuccessfulTransactions_WalletNotFound_ShouldReturnZero() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);
        when(walletService.getWalletByUserId(userId))
                .thenThrow(new IllegalArgumentException("Wallet not found"));

        long count = authService.countSuccessfulTransactions("test@example.com");

        assertEquals(0, count);
    }

    @Test
    void testCountSuccessfulTransactions_WithSuccessPayments_ShouldCountCorrectly() {
        Wallet wallet = new Wallet(userId);
        UUID walletId = UUID.randomUUID();
        wallet.setId(walletId);

        Transaction tx1 = new Transaction(walletId, TransactionType.PAYMENT, BigDecimal.TEN, "Payment");
        tx1.setStatus(TransactionStatus.SUCCESS);

        Transaction tx2 = new Transaction(walletId, TransactionType.PAYMENT, BigDecimal.TEN, "Payment");
        tx2.setStatus(TransactionStatus.SUCCESS);

        Transaction tx3 = new Transaction(walletId, TransactionType.TOP_UP, BigDecimal.TEN, "Top Up");
        tx3.setStatus(TransactionStatus.SUCCESS);

        Transaction tx4 = new Transaction(walletId, TransactionType.PAYMENT, BigDecimal.TEN, "Payment");
        tx4.setStatus(TransactionStatus.FAILED);

        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);
        when(walletService.getWalletByUserId(userId)).thenReturn(wallet);
        when(transactionRepository.findByWalletId(walletId)).thenReturn(List.of(tx1, tx2, tx3, tx4));

        long count = authService.countSuccessfulTransactions("test@example.com");

        assertEquals(2, count); // hanya tx1 dan tx2 yang SUCCESS PAYMENT
    }

    // ── addRating ─────────────────────────────────────────────────────────────

    @Test
    void testAddRating_ValidScore_ShouldUpdateRating() {
        sampleUser.setRating(4.0);
        sampleUser.setTotalReviews(1);
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.addRating("test@example.com", 5);

        assertNotNull(result);
        assertEquals(2, result.getTotalReviews());
        assertEquals(4.5, result.getRating());
    }

    @Test
    void testAddRating_FirstRating_ShouldSetRatingCorrectly() {
        sampleUser.setRating(0.0);
        sampleUser.setTotalReviews(0);
        when(userRepository.findByEmail("test@example.com")).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.addRating("test@example.com", 4);

        assertEquals(1, result.getTotalReviews());
        assertEquals(4.0, result.getRating());
    }

    @Test
    void testAddRating_UserNotFound_ShouldThrowException() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> authService.addRating("ghost@example.com", 5));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testAddRating_ScoreTooLow_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.addRating("test@example.com", 0));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testAddRating_ScoreTooHigh_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.addRating("test@example.com", 6));

        verify(userRepository, never()).save(any());
    }
}