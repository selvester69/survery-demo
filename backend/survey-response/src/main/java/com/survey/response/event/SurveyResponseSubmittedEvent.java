package com.survey.response.event;

public class SurveyResponseSubmittedEvent {
    private final Long responseId;

    public SurveyResponseSubmittedEvent(Long responseId) {
        this.responseId = responseId;
    }

    public Long getResponseId() {
        return responseId;
    }
}
