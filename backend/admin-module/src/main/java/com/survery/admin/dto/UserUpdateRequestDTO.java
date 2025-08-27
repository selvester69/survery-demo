package com.survery.admin.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateRequestDTO {

    @Size(max = 200, message = "Name must be less than 200 characters.")
    private String name;

    private Boolean active;

    //<editor-fold desc="Getters and Setters">
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    //</editor-fold>
}
