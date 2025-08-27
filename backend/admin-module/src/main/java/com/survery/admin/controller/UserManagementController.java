package com.survery.admin.controller;

import com.survery.admin.dto.AssignRoleRequestDTO;
import com.survery.admin.dto.UserCreationRequestDTO;
import com.survery.admin.dto.UserResponseDTO;
import com.survery.admin.dto.UserUpdateRequestDTO;
import com.survery.admin.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreationRequestDTO requestDTO) {
        UserResponseDTO createdUser = userManagementService.createUser(requestDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId) {
        UserResponseDTO user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserUpdateRequestDTO requestDTO) {
        UserResponseDTO updatedUser = userManagementService.updateUser(userId, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<UserResponseDTO> assignRoleToUser(@PathVariable UUID userId, @Valid @RequestBody AssignRoleRequestDTO requestDTO) {
        UserResponseDTO updatedUser = userManagementService.assignRoleToUser(userId, requestDTO.getRoleId());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserResponseDTO> removeRoleFromUser(@PathVariable UUID userId, @PathVariable UUID roleId) {
        UserResponseDTO updatedUser = userManagementService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(updatedUser);
    }
}
