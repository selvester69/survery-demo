package com.survery.admin.service;

import com.survery.admin.dto.UserCreationRequestDTO;
import com.survery.admin.dto.UserResponseDTO;
import com.survery.admin.dto.UserUpdateRequestDTO;
import com.survery.admin.exception.DuplicateResourceException;
import com.survery.admin.exception.ResourceNotFoundException;
import com.survery.admin.model.AdminUser;
import com.survery.admin.model.Role;
import com.survery.admin.model.UserRole;
import com.survery.admin.repository.AdminUserRepository;
import com.survery.admin.repository.RoleRepository;
import com.survery.admin.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserManagementServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserManagementService userManagementService;

    private AdminUser user;
    private Role role;
    private UserCreationRequestDTO creationRequestDTO;
    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        user = new AdminUser();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setUserRoles(new HashSet<>());

        role = new Role();
        role.setId(roleId);
        role.setName("ADMIN");

        creationRequestDTO = new UserCreationRequestDTO();
        creationRequestDTO.setEmail("newuser@example.com");
        creationRequestDTO.setName("New User");
    }

    @Test
    void whenCreateUser_withValidData_shouldSucceed() {
        // Arrange
        when(adminUserRepository.findByEmail(creationRequestDTO.getEmail())).thenReturn(Optional.empty());
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponseDTO result = userManagementService.createUser(creationRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(creationRequestDTO.getEmail(), result.getEmail());
        assertEquals(creationRequestDTO.getName(), result.getName());
        verify(adminUserRepository, times(1)).save(any(AdminUser.class));
    }

    @Test
    void whenCreateUser_withExistingEmail_shouldThrowDuplicateResourceException() {
        // Arrange
        when(adminUserRepository.findByEmail(creationRequestDTO.getEmail())).thenReturn(Optional.of(new AdminUser()));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            userManagementService.createUser(creationRequestDTO);
        });
        verify(adminUserRepository, never()).save(any(AdminUser.class));
    }

    @Test
    void whenCreateUser_withNonExistentRole_shouldThrowResourceNotFoundException() {
        // Arrange
        creationRequestDTO.setRoleIds(Set.of(UUID.randomUUID()));
        when(adminUserRepository.findByEmail(creationRequestDTO.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.createUser(creationRequestDTO);
        });
        verify(adminUserRepository, never()).save(any(AdminUser.class));
    }

    @Test
    void whenGetUserById_withExistingUser_shouldReturnUser() {
        // Arrange
        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponseDTO result = userManagementService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void whenGetUserById_withNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        when(adminUserRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.getUserById(userId);
        });
    }

    @Test
    void whenUpdateUser_withValidData_shouldSucceed() {
        // Arrange
        UserUpdateRequestDTO updateDTO = new UserUpdateRequestDTO();
        updateDTO.setName("Updated Name");
        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponseDTO result = userManagementService.updateUser(userId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(adminUserRepository, times(1)).save(user);
    }

    @Test
    void whenUpdateUser_withNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        UserUpdateRequestDTO updateDTO = new UserUpdateRequestDTO();
        when(adminUserRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.updateUser(userId, updateDTO);
        });
    }

    @Test
    void whenDeleteUser_withExistingUser_shouldSucceed() {
        // Arrange
        when(adminUserRepository.existsById(userId)).thenReturn(true);
        doNothing().when(adminUserRepository).deleteById(userId);

        // Act
        assertDoesNotThrow(() -> {
            userManagementService.deleteUser(userId);
        });

        // Assert
        verify(adminUserRepository, times(1)).deleteById(userId);
    }

    @Test
    void whenDeleteUser_withNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        when(adminUserRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.deleteUser(userId);
        });
        verify(adminUserRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void whenAssignRole_withValidUserAndRole_shouldSucceed() {
        // Arrange
        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(adminUserRepository.save(any(AdminUser.class))).thenReturn(user);

        // Act
        UserResponseDTO result = userManagementService.assignRoleToUser(userId, roleId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().contains("ADMIN"));
        verify(adminUserRepository, times(1)).save(user);
    }

    @Test
    void whenAssignRole_toNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        when(adminUserRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.assignRoleToUser(userId, roleId);
        });
    }

    @Test
    void whenAssignRole_withNonExistentRole_shouldThrowResourceNotFoundException() {
        // Arrange
        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.assignRoleToUser(userId, roleId);
        });
    }

    @Test
    void whenAssignRole_thatUserAlreadyHas_shouldThrowDuplicateResourceException() {
        // Arrange
        UserRole existingUserRole = new UserRole();
        existingUserRole.setUser(user);
        existingUserRole.setRole(role);
        user.getUserRoles().add(existingUserRole);
        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            userManagementService.assignRoleToUser(userId, roleId);
        });
    }

    @Test
    void whenRemoveRole_withValidUserAndRole_shouldSucceed() {
        // Arrange
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        user.getUserRoles().add(userRole);

        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adminUserRepository.save(any(AdminUser.class))).thenReturn(user);

        // Act
        UserResponseDTO result = userManagementService.removeRoleFromUser(userId, roleId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
        verify(adminUserRepository, times(1)).save(user);
    }

    @Test
    void whenRemoveRole_fromNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        when(adminUserRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.removeRoleFromUser(userId, roleId);
        });
    }

    @Test
    void whenRemoveRole_thatUserDoesNotHave_shouldThrowResourceNotFoundException() {
        // Arrange
        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(user)); // User has no roles

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userManagementService.removeRoleFromUser(userId, roleId);
        });
    }
}
