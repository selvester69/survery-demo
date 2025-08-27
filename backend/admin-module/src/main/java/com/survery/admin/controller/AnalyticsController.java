package com.survery.admin.controller;

import com.survery.admin.dto.analytics.SurveyAnalyticsDTO;
import com.survery.admin.service.AnalyticsViewingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/analytics")
public class AnalyticsController {

    private final AnalyticsViewingService analyticsViewingService;

    public AnalyticsController(AnalyticsViewingService analyticsViewingService) {
        this.analyticsViewingService = analyticsViewingService;
    }

    @GetMapping("/surveys/{surveyId}")
    public ResponseEntity<SurveyAnalyticsDTO> getSurveyAnalytics(@PathVariable UUID surveyId) {
        SurveyAnalyticsDTO analytics = analyticsViewingService.getAnalyticsForSurvey(surveyId);
        return ResponseEntity.ok(analytics);
    }
}
