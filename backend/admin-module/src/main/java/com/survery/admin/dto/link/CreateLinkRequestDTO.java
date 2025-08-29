package com.survery.admin.dto.link;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class CreateLinkRequestDTO {

    @NotBlank
    @URL
    private String longUrl;

    //<editor-fold desc="Getters and Setters">
    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
    //</editor-fold>
}
