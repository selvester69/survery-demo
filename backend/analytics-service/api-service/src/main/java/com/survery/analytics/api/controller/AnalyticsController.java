package com.survery.analytics.api.controller;

import com.survery.analytics.api.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/surveys/count")
    public ResponseEntity<Map<String, Long>> getSurveyCount() {
        long count = analyticsService.getSurveyCount();
        return ResponseEntity.ok(Map.of("totalSurveys", count));
    }
}
