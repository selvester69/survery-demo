package com.survey.response.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SurveyResponseRequest {
    @NotNull(message = "Survey ID is required")
    private Long surveyId;

    private String respondentId;

    @NotNull(message = "Link ID is required")
    private String linkId;

    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<QuestionAnswerRequest> answers;

    // Getters and Setters
    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public String getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(String respondentId) {
        this.respondentId = respondentId;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public List<QuestionAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswerRequest> answers) {
        this.answers = answers;
    }
}
