package com.survery.admin.dto;

import com.survery.admin.model.SurveyStatus;
import jakarta.validation.constraints.Size;

public class SurveyUpdateRequestDTO {

    @Size(max = 255, message = "Survey title cannot exceed 255 characters.")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters.")
    private String description;

    private SurveyStatus status;

    //<editor-fold desc="Getters and Setters">
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SurveyStatus getStatus() {
        return status;
    }

    public void setStatus(SurveyStatus status) {
        this.status = status;
    }
    //</editor-fold>
}
