package com.survery.analytics.transformer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformedSurveyDto {
    // Original fields
    private UUID surveyId;
    private String questionId;
    private Map<String, Object> response;
    private UUID userId;
    private OffsetDateTime timestamp;
    private double lat;
    private double lon;

    // Enriched fields
    private UUID villageId;
    private UUID panchayatId;
    private UUID constituencyId;
}
