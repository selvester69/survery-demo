package com.survery.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SurveyCreationRequestDTO {

    @NotBlank(message = "Survey title cannot be blank.")
    @Size(max = 255, message = "Survey title cannot exceed 255 characters.")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters.")
    private String description;

    @Valid
    private List<QuestionRequestDTO> questions;

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

    public List<QuestionRequestDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionRequestDTO> questions) {
        this.questions = questions;
    }
    //</editor-fold>
}
