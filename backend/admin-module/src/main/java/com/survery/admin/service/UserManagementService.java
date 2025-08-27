package com.survery.admin.service;

import com.survery.admin.dto.AssignRoleRequestDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private final AdminUserRepository adminUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserManagementService(AdminUserRepository adminUserRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.adminUserRepository = adminUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public UserResponseDTO createUser(UserCreationRequestDTO requestDTO) {
        adminUserRepository.findByEmail(requestDTO.getEmail()).ifPresent(u -> {
            throw new DuplicateResourceException("User with email " + requestDTO.getEmail() + " already exists.");
        });

        AdminUser newUser = new AdminUser();
        newUser.setEmail(requestDTO.getEmail());
        newUser.setName(requestDTO.getName());

        if (requestDTO.getRoleIds() != null && !requestDTO.getRoleIds().isEmpty()) {
            Set<UserRole> assignedRoles = new HashSet<>();
            for (UUID roleId : requestDTO.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
                UserRole userRole = new UserRole();
                userRole.setUser(newUser);
                userRole.setRole(role);
                assignedRoles.add(userRole);
            }
            newUser.setUserRoles(assignedRoles);
        }

        AdminUser savedUser = adminUserRepository.save(newUser);
        return toUserResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return toUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(UUID userId, UserUpdateRequestDTO requestDTO) {
        AdminUser userToUpdate = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (requestDTO.getName() != null) {
            userToUpdate.setName(requestDTO.getName());
        }
        if (requestDTO.getActive() != null) {
            userToUpdate.setActive(requestDTO.getActive());
        }

        AdminUser updatedUser = adminUserRepository.save(userToUpdate);
        return toUserResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!adminUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        adminUserRepository.deleteById(userId);
    }

    @Transactional
    public UserResponseDTO assignRoleToUser(UUID userId, UUID roleId) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        boolean alreadyHasRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getId().equals(roleId));

        if (alreadyHasRole) {
            throw new DuplicateResourceException("User already has the role: " + role.getName());
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        user.getUserRoles().add(userRole);

        AdminUser updatedUser = adminUserRepository.save(user);
        return toUserResponseDTO(updatedUser);
    }

    @Transactional
    public UserResponseDTO removeRoleFromUser(UUID userId, UUID roleId) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        UserRole userRoleToRemove = user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User does not have the specified role."));

        // Because of 'orphanRemoval = true' on the userRoles mapping,
        // removing the UserRole from the set will trigger its deletion.
        user.getUserRoles().remove(userRoleToRemove);

        // Saving the user will cascade the changes.
        AdminUser updatedUser = adminUserRepository.save(user);
        return toUserResponseDTO(updatedUser);
    }

    private UserResponseDTO toUserResponseDTO(AdminUser user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        if (user.getUserRoles() != null) {
            Set<String> roleNames = user.getUserRoles().stream()
                    .map(userRole -> userRole.getRole().getName())
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        }
        return dto;
    }
}
