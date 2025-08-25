package com.survery.links;

import com.survery.links.dto.CreateLinkRequest;
import com.survery.links.dto.LinkResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private SurveyLinkRepository surveyLinkRepository;

    @Mock
    private LinkMapper linkMapper;

    @InjectMocks
    private LinkService linkService;

    private CreateLinkRequest createLinkRequest;

    @BeforeEach
    void setUp() {
        createLinkRequest = new CreateLinkRequest();
        createLinkRequest.setSurveyId(UUID.randomUUID());
        createLinkRequest.setTargetUrl("https://example.com/survey/123");
    }

    @Test
    void createLink_shouldSaveAndReturnLinkResponse() {
        // Arrange
        ArgumentCaptor<SurveyLink> surveyLinkCaptor = ArgumentCaptor.forClass(SurveyLink.class);

        // Mock the repository save operation
        when(surveyLinkRepository.save(any(SurveyLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock the mapper
        when(linkMapper.toResponse(any(SurveyLink.class))).thenAnswer(invocation -> {
            SurveyLink savedLink = invocation.getArgument(0);
            return LinkResponse.builder()
                    .linkId(savedLink.getLinkId())
                    .surveyId(savedLink.getSurveyId())
                    .targetUrl(savedLink.getTargetUrl())
                    .build();
        });

        // Act
        LinkResponse result = linkService.createLink(createLinkRequest);

        // Assert
        verify(surveyLinkRepository).save(surveyLinkCaptor.capture());
        SurveyLink savedSurveyLink = surveyLinkCaptor.getValue();

        assertEquals(createLinkRequest.getSurveyId(), savedSurveyLink.getSurveyId());
        assertEquals(createLinkRequest.getTargetUrl(), savedSurveyLink.getTargetUrl());
        assertEquals(savedSurveyLink.getLinkId(), result.getLinkId());
    }
}
