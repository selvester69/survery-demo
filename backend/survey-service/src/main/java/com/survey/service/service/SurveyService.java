package com.survey.service.service;

import com.survey.service.dto.QuestionCreateDTO;
import com.survey.service.dto.SurveyCreateDTO;
import com.survey.service.event.SurveyCreatedEvent;
import com.survey.service.model.Question;
import com.survey.service.model.Survey;
import com.survey.service.model.SurveyStatus;
import com.survey.service.repository.QuestionRepository;
import com.survey.service.repository.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Survey createSurvey(SurveyCreateDTO createDTO, UUID createdBy) {
        Survey survey = new Survey();
        survey.setTitle(createDTO.getTitle());
        survey.setDescription(createDTO.getDescription());
        survey.setWelcomeMessage(createDTO.getWelcomeMessage());
        survey.setThankYouMessage(createDTO.getThankYouMessage());
        survey.setSettings(createDTO.getSettings());
        survey.setExpiresAt(createDTO.getExpiresAt());
        survey.setCollectLocation(createDTO.getCollectLocation());
        survey.setRequireLocation(createDTO.getRequireLocation());
        survey.setCreatedBy(createdBy);
        survey.setStatus(SurveyStatus.DRAFT);

        Survey savedSurvey = surveyRepository.save(survey);

        if (createDTO.getQuestions() != null && !createDTO.getQuestions().isEmpty()) {
            createDTO.getQuestions().forEach(questionDTO -> createQuestion(savedSurvey.getId(), questionDTO));
        }

        // Publish survey created event
        eventPublisher.publishEvent(new SurveyCreatedEvent(savedSurvey));

        return savedSurvey;
    }

    @Transactional
    public Question createQuestion(UUID surveyId, QuestionCreateDTO createDTO) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found"));

        Question question = new Question();
        question.setSurvey(survey);
        question.setQuestionType(createDTO.getQuestionType());
        question.setQuestionText(createDTO.getQuestionText());
        question.setQuestionConfig(createDTO.getQuestionConfig());
        question.setValidationRules(createDTO.getValidationRules());
        question.setConditionalLogic(createDTO.getConditionalLogic());
        question.setOrderIndex(createDTO.getOrderIndex());
        question.setIsRequired(createDTO.getIsRequired());

        return questionRepository.save(question);
    }

    public Survey getSurveyById(UUID surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found"));
    }

    public Page<Survey> getAllSurveys(UUID ownerId, Pageable pageable) {
        return surveyRepository.findAll(pageable);
    }

    public List<Question> getQuestionsBySurveyId(UUID surveyId) {
        return questionRepository.findBySurveyIdOrderByOrderIndex(surveyId);
    }

    @Transactional
    public void deleteSurvey(UUID surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new EntityNotFoundException("Survey not found"));
        surveyRepository.delete(survey);
    }

    @Transactional
    public void deleteQuestion(UUID surveyId, UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        if (!question.getSurvey().getId().equals(surveyId)) {
            throw new IllegalArgumentException("Question does not belong to the specified survey");
        }

        questionRepository.delete(question);
    }

    public boolean validateSurveyAccess(UUID surveyId, UUID userId) {
        Survey survey = getSurveyById(surveyId);
        return survey.getCreatedBy().equals(userId);
    }
}
