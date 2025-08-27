package com.survery.admin.dto;

import java.util.UUID;

public class UserSummaryDTO {
    private UUID id;
    private String email;
    private String name;

    public UserSummaryDTO(UUID id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    //<editor-fold desc="Getters and Setters">
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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
    //</editor-fold>
}
