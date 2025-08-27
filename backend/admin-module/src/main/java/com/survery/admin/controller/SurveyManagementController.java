package com.survery.admin.controller;

import com.survery.admin.dto.SurveyCreationRequestDTO;
import com.survery.admin.dto.SurveyResponseDTO;
import com.survery.admin.dto.SurveyUpdateRequestDTO;
import com.survery.admin.service.SurveyManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/surveys")
public class SurveyManagementController {

    private final SurveyManagementService surveyManagementService;

    public SurveyManagementController(SurveyManagementService surveyManagementService) {
        this.surveyManagementService = surveyManagementService;
    }

    @PostMapping
    public ResponseEntity<SurveyResponseDTO> createSurvey(@Valid @RequestBody SurveyCreationRequestDTO requestDTO,
                                                          @RequestParam UUID createdByUserId) {
        // In a real app, createdByUserId would come from the security context
        SurveyResponseDTO createdSurvey = surveyManagementService.createSurvey(requestDTO, createdByUserId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSurvey.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdSurvey);
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyResponseDTO> getSurveyById(@PathVariable UUID surveyId) {
        SurveyResponseDTO survey = surveyManagementService.getSurveyById(surveyId);
        return ResponseEntity.ok(survey);
    }

    @GetMapping
    public ResponseEntity<List<SurveyResponseDTO>> getSurveysByUser(@RequestParam UUID userId) {
        List<SurveyResponseDTO> surveys = surveyManagementService.getSurveysByUserId(userId);
        return ResponseEntity.ok(surveys);
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<SurveyResponseDTO> updateSurvey(@PathVariable UUID surveyId,
                                                          @Valid @RequestBody SurveyUpdateRequestDTO requestDTO) {
        SurveyResponseDTO updatedSurvey = surveyManagementService.updateSurvey(surveyId, requestDTO);
        return ResponseEntity.ok(updatedSurvey);
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable UUID surveyId) {
        surveyManagementService.deleteSurvey(surveyId);
        return ResponseEntity.noContent().build();
    }
}
