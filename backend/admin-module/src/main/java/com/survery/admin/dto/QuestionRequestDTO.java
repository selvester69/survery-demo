package com.survery.admin.dto;

import com.survery.admin.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QuestionRequestDTO {

    @NotBlank(message = "Question text cannot be blank.")
    @Size(max = 1000, message = "Question text cannot exceed 1000 characters.")
    private String questionText;

    @NotNull(message = "Question type is required.")
    private QuestionType questionType;

    @NotNull(message = "Question order is required.")
    private Integer questionOrder;

    private boolean required = false;

    // JSON string for options
    private String options;

    //<editor-fold desc="Getters and Setters">
    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }
    //</editor-fold>
}
