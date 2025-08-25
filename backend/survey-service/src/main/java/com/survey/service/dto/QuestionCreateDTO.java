package com.survey.service.dto;

import com.survey.service.model.QuestionType;
import lombok.Data;

@Data
public class QuestionCreateDTO {
    private QuestionType questionType;
    private String questionText;
    private Object questionConfig;
    private Object validationRules;
    private Object conditionalLogic;
    private Integer orderIndex;
    private Boolean isRequired;
}
