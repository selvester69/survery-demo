package com.survery.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public class UserCreationRequestDTO {

    @NotBlank(message = "Email is required.")
    @Email(message = "Email should be valid.")
    @Size(max = 254)
    private String email;

    @NotBlank(message = "Name is required.")
    @Size(max = 200)
    private String name;

    private Set<UUID> roleIds;

    //<editor-fold desc="Getters and Setters">
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UUID> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<UUID> roleIds) {
        this.roleIds = roleIds;
    }
    //</editor-fold>
}
