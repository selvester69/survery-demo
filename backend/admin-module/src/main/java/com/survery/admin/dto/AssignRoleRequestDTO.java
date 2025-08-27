package com.survery.admin.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AssignRoleRequestDTO {

    @NotNull(message = "Role ID cannot be null.")
    private UUID roleId;

    //<editor-fold desc="Getters and Setters">
    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }
    //</editor-fold>
}
