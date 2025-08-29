package com.survery.admin.dto.analytics;

import java.util.List;
import java.util.UUID;

public class QuestionAnalyticsDTO {

    private UUID questionId;
    private String questionText;
    private long totalResponses;
    private List<AnswerAnalyticsDTO> answers;

    //<editor-fold desc="Getters and Setters">
    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public long getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(long totalResponses) {
        this.totalResponses = totalResponses;
    }

    public List<AnswerAnalyticsDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerAnalyticsDTO> answers) {
        this.answers = answers;
    }
    //</editor-fold>
}
