package com.survey.response.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question_answers")
public class QuestionAnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "response_id", nullable = false)
    private Long responseId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "answer_value")
    private String answerValue;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @ManyToOne
    @JoinColumn(name = "response_id", insertable = false, updatable = false)
    private SurveyResponseEntity surveyResponse;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResponseId() {
        return responseId;
    }

    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(String answerValue) {
        this.answerValue = answerValue;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public SurveyResponseEntity getSurveyResponse() {
        return surveyResponse;
    }

    public void setSurveyResponse(SurveyResponseEntity surveyResponse) {
        this.surveyResponse = surveyResponse;
    }
}
