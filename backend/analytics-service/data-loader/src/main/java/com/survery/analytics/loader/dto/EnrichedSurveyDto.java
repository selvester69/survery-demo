package com.survery.analytics.loader.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnrichedSurveyDto {
    private UUID surveyId;
    private String questionId;
    private Map<String, Object> response;
    private UUID userId;
    private OffsetDateTime timestamp;
    private double lat;
    private double lon;
    private UUID villageId;
    private UUID panchayatId;
    private UUID constituencyId;
}
