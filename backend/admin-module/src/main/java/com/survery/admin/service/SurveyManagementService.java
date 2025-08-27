package com.survery.admin.service;

import com.survery.admin.dto.*;
import com.survery.admin.exception.ResourceNotFoundException;
import com.survery.admin.model.*;
import com.survery.admin.repository.AdminUserRepository;
import com.survery.admin.repository.QuestionRepository;
import com.survery.admin.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SurveyManagementService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final AdminUserRepository adminUserRepository;

    public SurveyManagementService(SurveyRepository surveyRepository, QuestionRepository questionRepository, AdminUserRepository adminUserRepository) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Transactional
    public SurveyResponseDTO createSurvey(SurveyCreationRequestDTO requestDTO, UUID createdByUserId) {
        AdminUser creator = adminUserRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + createdByUserId));

        Survey survey = new Survey();
        survey.setTitle(requestDTO.getTitle());
        survey.setDescription(requestDTO.getDescription());
        survey.setCreatedBy(creator);

        if (requestDTO.getQuestions() != null) {
            List<Question> questions = requestDTO.getQuestions().stream()
                    .map(qDto -> {
                        Question question = new Question();
                        question.setSurvey(survey);
                        question.setQuestionText(qDto.getQuestionText());
                        question.setQuestionType(qDto.getQuestionType());
                        question.setQuestionOrder(qDto.getQuestionOrder());
                        question.setRequired(qDto.isRequired());
                        question.setOptions(qDto.getOptions());
                        return question;
                    }).collect(Collectors.toList());
            survey.getQuestions().addAll(questions);
        }

        Survey savedSurvey = surveyRepository.save(survey);
        return toSurveyResponseDTO(savedSurvey);
    }

    @Transactional(readOnly = true)
    public SurveyResponseDTO getSurveyById(UUID surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + surveyId));
        return toSurveyResponseDTO(survey);
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseDTO> getSurveysByUserId(UUID userId) {
        if (!adminUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return surveyRepository.findByCreatedById(userId).stream()
                .map(this::toSurveyResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SurveyResponseDTO updateSurvey(UUID surveyId, SurveyUpdateRequestDTO requestDTO) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + surveyId));

        if (requestDTO.getTitle() != null) {
            survey.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getDescription() != null) {
            survey.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getStatus() != null) {
            survey.setStatus(requestDTO.getStatus());
        }

        Survey updatedSurvey = surveyRepository.save(survey);
        return toSurveyResponseDTO(updatedSurvey);
    }

    @Transactional
    public void deleteSurvey(UUID surveyId) {
        if (!surveyRepository.existsById(surveyId)) {
            throw new ResourceNotFoundException("Survey not found with ID: " + surveyId);
        }
        surveyRepository.deleteById(surveyId);
    }


    // --- Private Mapper Methods ---

    private SurveyResponseDTO toSurveyResponseDTO(Survey survey) {
        SurveyResponseDTO dto = new SurveyResponseDTO();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setStatus(survey.getStatus());
        dto.setCreatedAt(survey.getCreatedAt());
        dto.setUpdatedAt(survey.getUpdatedAt());

        if (survey.getCreatedBy() != null) {
            dto.setCreatedBy(toUserSummaryDTO(survey.getCreatedBy()));
        }

        if (survey.getQuestions() != null) {
            dto.setQuestions(survey.getQuestions().stream()
                    .map(this::toQuestionResponseDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private QuestionResponseDTO toQuestionResponseDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setQuestionOrder(question.getQuestionOrder());
        dto.setRequired(question.isRequired());
        dto.setOptions(question.getOptions());
        return dto;
    }

    private UserSummaryDTO toUserSummaryDTO(AdminUser user) {
        return new UserSummaryDTO(user.getId(), user.getEmail(), user.getName());
    }
}
