package com.survey.service.controller;

import com.survey.service.dto.QuestionCreateDTO;
import com.survey.service.dto.SurveyCreateDTO;
import com.survey.service.model.Question;
import com.survey.service.model.Survey;
import com.survey.service.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {
    private final SurveyService surveyService;

    @PostMapping
    public ResponseEntity<Survey> createSurvey(
            @RequestBody @Valid SurveyCreateDTO createDTO,
            @RequestHeader("X-User-ID") UUID userId) {
        Survey survey = surveyService.createSurvey(createDTO, userId);
        return ResponseEntity.ok(survey);
    }

    @GetMapping
    public ResponseEntity<Page<Survey>> getAllSurveys(
            @RequestHeader("X-User-ID") UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(surveyService.getAllSurveys(userId, pageable));
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<Survey> getSurvey(
            @PathVariable UUID surveyId,
            @RequestHeader("X-User-ID") UUID userId) {
        if (!surveyService.validateSurveyAccess(surveyId, userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(surveyService.getSurveyById(surveyId));
    }

    @PostMapping("/{surveyId}/questions")
    public ResponseEntity<Question> addQuestion(
            @PathVariable UUID surveyId,
            @RequestBody @Valid QuestionCreateDTO createDTO,
            @RequestHeader("X-User-ID") UUID userId) {
        if (!surveyService.validateSurveyAccess(surveyId, userId)) { // Changed from forbidden() to status(403)
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(surveyService.createQuestion(surveyId, createDTO));
    }

    @GetMapping("/{surveyId}/questions")
    public ResponseEntity<List<Question>> getQuestions(
            @PathVariable UUID surveyId,
            @RequestHeader("X-User-ID") UUID userId) {
        if (!surveyService.validateSurveyAccess(surveyId, userId)) { // Changed from forbidden() to status(403)
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(surveyService.getQuestionsBySurveyId(surveyId));
    }

    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(
            @PathVariable UUID surveyId,
            @RequestHeader("X-User-ID") UUID userId) {
        if (!surveyService.validateSurveyAccess(surveyId, userId)) { // Changed from forbidden() to status(403)
            return ResponseEntity.status(403).build();
        }
        surveyService.deleteSurvey(surveyId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{surveyId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable UUID surveyId,
            @PathVariable UUID questionId,
            @RequestHeader("X-User-ID") UUID userId) {
        if (!surveyService.validateSurveyAccess(surveyId, userId)) { // Changed from forbidden() to status(403)
            return ResponseEntity.status(403).build();
        }
        surveyService.deleteQuestion(surveyId, questionId);
        return ResponseEntity.noContent().build();
    }
}
