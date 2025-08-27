package com.survery.admin.service;

import com.survery.admin.dto.SurveyCreationRequestDTO;
import com.survery.admin.dto.SurveyResponseDTO;
import com.survery.admin.dto.SurveyUpdateRequestDTO;
import com.survery.admin.exception.ResourceNotFoundException;
import com.survery.admin.model.AdminUser;
import com.survery.admin.model.Survey;
import com.survery.admin.model.SurveyStatus;
import com.survery.admin.repository.AdminUserRepository;
import com.survery.admin.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SurveyManagementServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private SurveyManagementService surveyManagementService;

    private AdminUser creator;
    private Survey survey;
    private UUID surveyId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        creator = new AdminUser();
        creator.setId(userId);
        creator.setName("Test Admin");
        creator.setEmail("admin@test.com");

        surveyId = UUID.randomUUID();
        survey = new Survey();
        survey.setId(surveyId);
        survey.setTitle("Test Survey");
        survey.setCreatedBy(creator);
    }

    @Test
    void whenCreateSurvey_withValidUser_shouldSucceed() {
        // Arrange
        SurveyCreationRequestDTO dto = new SurveyCreationRequestDTO();
        dto.setTitle("New Survey");
        when(adminUserRepository.findById(userId)).thenReturn(Optional.of(creator));
        when(surveyRepository.save(any(Survey.class))).thenReturn(survey);

        // Act
        SurveyResponseDTO result = surveyManagementService.createSurvey(dto, userId);

        // Assert
        assertNotNull(result);
        assertEquals(survey.getId(), result.getId());
        verify(surveyRepository, times(1)).save(any(Survey.class));
    }

    @Test
    void whenCreateSurvey_withNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        SurveyCreationRequestDTO dto = new SurveyCreationRequestDTO();
        when(adminUserRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            surveyManagementService.createSurvey(dto, userId);
        });
    }

    @Test
    void whenGetSurveyById_withExistingSurvey_shouldReturnSurvey() {
        // Arrange
        when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(survey));

        // Act
        SurveyResponseDTO result = surveyManagementService.getSurveyById(surveyId);

        // Assert
        assertNotNull(result);
        assertEquals(surveyId, result.getId());
    }

    @Test
    void whenGetSurveyById_withNonExistentSurvey_shouldThrowResourceNotFoundException() {
        // Arrange
        when(surveyRepository.findById(surveyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            surveyManagementService.getSurveyById(surveyId);
        });
    }

    @Test
    void whenUpdateSurvey_withValidData_shouldSucceed() {
        // Arrange
        SurveyUpdateRequestDTO dto = new SurveyUpdateRequestDTO();
        dto.setTitle("Updated Title");
        dto.setStatus(SurveyStatus.ACTIVE);
        when(surveyRepository.findById(surveyId)).thenReturn(Optional.of(survey));
        when(surveyRepository.save(any(Survey.class))).thenReturn(survey);

        // Act
        SurveyResponseDTO result = surveyManagementService.updateSurvey(surveyId, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(SurveyStatus.ACTIVE, result.getStatus());
        verify(surveyRepository, times(1)).save(survey);
    }

    @Test
    void whenDeleteSurvey_withExistingSurvey_shouldSucceed() {
        // Arrange
        when(surveyRepository.existsById(surveyId)).thenReturn(true);
        doNothing().when(surveyRepository).deleteById(surveyId);

        // Act & Assert
        assertDoesNotThrow(() -> surveyManagementService.deleteSurvey(surveyId));
        verify(surveyRepository, times(1)).deleteById(surveyId);
    }

    @Test
    void whenGetSurveysByUserId_withExistingUser_shouldReturnList() {
        // Arrange
        when(adminUserRepository.existsById(userId)).thenReturn(true);
        when(surveyRepository.findByCreatedById(userId)).thenReturn(Collections.singletonList(survey));

        // Act
        List<SurveyResponseDTO> result = surveyManagementService.getSurveysByUserId(userId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(surveyId, result.get(0).getId());
    }

    @Test
    void whenGetSurveysByUserId_withNonExistentUser_shouldThrowResourceNotFoundException() {
        // Arrange
        when(adminUserRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            surveyManagementService.getSurveysByUserId(userId);
        });
    }
}
