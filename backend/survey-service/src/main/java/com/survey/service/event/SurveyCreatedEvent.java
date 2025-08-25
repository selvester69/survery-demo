package com.survey.service.event;

import com.survey.service.model.Survey;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SurveyCreatedEvent extends ApplicationEvent {
    private final Survey survey;

    public SurveyCreatedEvent(Survey survey) {
        super(survey);
        this.survey = survey;
    }
}
