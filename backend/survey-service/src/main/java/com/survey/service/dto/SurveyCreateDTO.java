package com.survey.service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SurveyCreateDTO {
    private String title;
    private String description;
    private String welcomeMessage;
    private String thankYouMessage;
    private Object settings;
    private LocalDateTime expiresAt;
    private Boolean collectLocation;
    private Boolean requireLocation;
    private List<QuestionCreateDTO> questions;
}
