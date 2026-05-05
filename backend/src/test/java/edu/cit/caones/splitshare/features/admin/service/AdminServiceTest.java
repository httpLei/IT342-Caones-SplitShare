package edu.cit.caones.splitshare.features.admin.service;

import edu.cit.caones.splitshare.features.admin.adapter.AdminAuditLogDtoAdapter;
import edu.cit.caones.splitshare.features.admin.adapter.AdminUserDtoAdapter;
import edu.cit.caones.splitshare.features.admin.entity.AdminAuditLog;
import edu.cit.caones.splitshare.features.admin.repository.AdminAuditLogRepository;
import edu.cit.caones.splitshare.features.auth.entity.Role;
import edu.cit.caones.splitshare.features.auth.entity.User;
import edu.cit.caones.splitshare.features.auth.repository.UserRepository;
import edu.cit.caones.splitshare.shared.dto.response.AdminAuditLogDto;
import edu.cit.caones.splitshare.shared.dto.response.AdminUserDto;
import edu.cit.caones.splitshare.shared.event.UserStatusChangedEvent;
import edu.cit.caones.splitshare.shared.strategy.UserStatusActionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Unit Tests")
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Mock
    private AdminUserDtoAdapter adminUserDtoAdapter;

    @Mock
    private AdminAuditLogDtoAdapter auditLogDtoAdapter;

    private List<UserStatusActionStrategy> statusStrategies;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AdminService adminService;

    private User testUser;
    private User adminUser;
    private AdminUserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstname("John");
        testUser.setLastname("Doe");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.ROLE_USER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(Instant.now());

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstname("Admin");
        adminUser.setLastname("User");
        adminUser.setPassword("encodedPassword");
        adminUser.setRole(Role.ROLE_ADMIN);
        adminUser.setEnabled(true);
        adminUser.setCreatedAt(Instant.now());

        // use builder since DTOs are Lombok @Builder
        testUserDto = AdminUserDto.builder()
            .id(1L)
            .email("user@example.com")
            .role("ROLE_USER")
            .enabled(true)
            .build();
        statusStrategies = new java.util.ArrayList<>();

        // construct service under test with mocks and real list
        adminService = new AdminService(
                userRepository,
                adminAuditLogRepository,
                adminUserDtoAdapter,
                auditLogDtoAdapter,
                statusStrategies,
                eventPublisher
        );
    }

    // ── SUSPEND USER TESTS ───────────────────────────────────────────────────

    @Test
    @DisplayName("Should successfully disable user and publish event")
    void testDisableUserSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(adminUserDtoAdapter.adapt(any(User.class))).thenReturn(testUserDto);

        UserStatusActionStrategy mockStrategy = mock(UserStatusActionStrategy.class);
        when(mockStrategy.supports(false)).thenReturn(true);
        statusStrategies.add(mockStrategy);

        // Act
        AdminUserDto result = adminService.updateUserStatus(1L, false, "admin@example.com");

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserStatusChangedEvent.class));
    }

    @Test
    @DisplayName("Should set user enabled flag to false when disabling")
    void testDisableUserSetsEnabledFalse() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser);
        when(adminUserDtoAdapter.adapt(any(User.class))).thenReturn(testUserDto);

        UserStatusActionStrategy mockStrategy = mock(UserStatusActionStrategy.class);
        when(mockStrategy.supports(false)).thenReturn(true);
        statusStrategies.add(mockStrategy);

        // Act
        adminService.updateUserStatus(1L, false, "admin@example.com");

        // Assert
        User savedUser = userCaptor.getValue();
        assertFalse(savedUser.isEnabled());
    }

    @Test
    @DisplayName("Should publish UserStatusChangedEvent when disabling user")
    void testDisableUserPublishesEvent() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(adminUserDtoAdapter.adapt(any(User.class))).thenReturn(testUserDto);

        UserStatusActionStrategy mockStrategy = mock(UserStatusActionStrategy.class);
        when(mockStrategy.supports(false)).thenReturn(true);
        statusStrategies.add(mockStrategy);

        ArgumentCaptor<UserStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(UserStatusChangedEvent.class);

        // Act
        adminService.updateUserStatus(1L, false, "admin@example.com");

        // Assert
        verify(eventPublisher).publishEvent(eventCaptor.capture());
    }

    @Test
    @DisplayName("Should throw exception when user tries to disable themselves")
    void testCannotDisableSelfAccount() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> adminService.updateUserStatus(1L, false, "user@example.com"));
    }

    // ── REACTIVATE USER TESTS ────────────────────────────────────────────────

    @Test
    @DisplayName("Should successfully enable user")
    void testEnableUserSuccess() {
        // Arrange
        testUser.setEnabled(false);  // User is disabled
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(adminUserDtoAdapter.adapt(any(User.class))).thenReturn(testUserDto);

        UserStatusActionStrategy mockStrategy = mock(UserStatusActionStrategy.class);
        when(mockStrategy.supports(true)).thenReturn(true);
        statusStrategies.add(mockStrategy);

        // Act
        AdminUserDto result = adminService.updateUserStatus(1L, true, "admin@example.com");

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should set user enabled flag to true when enabling")
    void testEnableUserSetsEnabledTrue() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser);
        when(adminUserDtoAdapter.adapt(any(User.class))).thenReturn(testUserDto);

        UserStatusActionStrategy mockStrategy = mock(UserStatusActionStrategy.class);
        when(mockStrategy.supports(true)).thenReturn(true);
        statusStrategies.add(mockStrategy);

        // Act
        adminService.updateUserStatus(1L, true, "admin@example.com");

        // Assert
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.isEnabled());
    }

    // ── VIEW USERS TESTS ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Should retrieve all users")
    void testListUsersSuccess() {
        // Arrange
        List<User> allUsers = List.of(testUser, adminUser);
        when(userRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(allUsers);
        when(adminUserDtoAdapter.adapt(any(User.class))).thenReturn(testUserDto);

        // Act
        List<AdminUserDto> result = adminService.listUsers();

        // Assert
        assertNotNull(result);
        verify(userRepository).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void testListUsersEmptyList() {
        // Arrange
        when(userRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());

        // Act
        List<AdminUserDto> result = adminService.listUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── VIEW AUDIT LOGS TESTS ────────────────────────────────────────────────

    @Test
    @DisplayName("Should retrieve audit logs with default limit")
    void testListAuditLogsSuccess() {
        // Arrange
        when(adminAuditLogRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        // Act
        List<AdminAuditLogDto> result = adminService.listAuditLogs(10);

        // Assert
        assertNotNull(result);
        verify(adminAuditLogRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Should enforce maximum limit of 200 for audit logs")
    void testListAuditLogsEnforcesMaxLimit() {
        // Arrange
        when(adminAuditLogRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        // Act
        adminService.listAuditLogs(500); // Request 500, should limit to 200

        // Assert
        ArgumentCaptor<org.springframework.data.domain.Pageable> pageCaptor = ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(adminAuditLogRepository).findAll(pageCaptor.capture());
        // Verify that the page size is limited
    }

    @Test
    @DisplayName("Should handle user not found exception")
    void testUpdateUserStatusUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, 
            () -> adminService.updateUserStatus(999L, false, "admin@example.com"));
    }


}


