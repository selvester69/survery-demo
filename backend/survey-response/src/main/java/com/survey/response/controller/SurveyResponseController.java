package com.survey.response.controller;

import com.survey.response.dto.SurveyResponseRequest;
import com.survey.response.service.SurveyResponseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyResponseController {
    private final SurveyResponseService surveyResponseService;

    @Autowired
    public SurveyResponseController(SurveyResponseService surveyResponseService) {
        this.surveyResponseService = surveyResponseService;
    }

    @PostMapping("/{linkId}/submit")
    public ResponseEntity<Map<String, Object>> submitSurveyResponse(
            @PathVariable String linkId,
            @Valid @RequestBody SurveyResponseRequest request) {
        request.setLinkId(linkId);
        Long responseId = surveyResponseService.processSubmission(request);

        Map<String, Object> response = new HashMap<>();
        response.put("responseId", responseId);
        response.put("message", "Survey response submitted successfully");

        return ResponseEntity.ok(response);
    }
}
