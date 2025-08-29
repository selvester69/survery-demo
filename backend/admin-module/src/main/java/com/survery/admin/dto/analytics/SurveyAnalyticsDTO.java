package com.survery.admin.dto.analytics;

import java.util.List;
import java.util.UUID;

public class SurveyAnalyticsDTO {

    private UUID surveyId;
    private String surveyTitle;
    private long totalSubmissions;
    private double completionRate;
    private List<QuestionAnalyticsDTO> questions;

    //<editor-fold desc="Getters and Setters">
    public UUID getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(UUID surveyId) {
        this.surveyId = surveyId;
    }

    public String getSurveyTitle() {
        return surveyTitle;
    }

    public void setSurveyTitle(String surveyTitle) {
        this.surveyTitle = surveyTitle;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public List<QuestionAnalyticsDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionAnalyticsDTO> questions) {
        this.questions = questions;
    }
    //</editor-fold>
}
