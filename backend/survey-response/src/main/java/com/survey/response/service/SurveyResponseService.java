package com.survey.response.service;

import com.survey.response.dto.SurveyResponseRequest;
import com.survey.response.entity.QuestionAnswerEntity;
import com.survey.response.entity.SurveyResponseEntity;
import com.survey.response.event.SurveyResponseSubmittedEvent;
import com.survey.response.repository.QuestionAnswerRepository;
import com.survey.response.repository.SurveyResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyResponseService {
    private final SurveyResponseRepository surveyResponseRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public SurveyResponseService(
            SurveyResponseRepository surveyResponseRepository,
            QuestionAnswerRepository questionAnswerRepository,
            ApplicationEventPublisher eventPublisher) {
        this.surveyResponseRepository = surveyResponseRepository;
        this.questionAnswerRepository = questionAnswerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long processSubmission(SurveyResponseRequest request) {
        // Create and save the survey response
        SurveyResponseEntity response = new SurveyResponseEntity();
        response.setSurveyId(request.getSurveyId());
        response.setRespondentId(request.getRespondentId());
        response.setLinkId(request.getLinkId());
        response.setStartedAt(LocalDateTime.now());
        response.setCompletedAt(LocalDateTime.now());

        SurveyResponseEntity savedResponse = surveyResponseRepository.save(response);

        // Process and save all answers
        List<QuestionAnswerEntity> answers = request.getAnswers().stream()
                .map(answerRequest -> {
                    QuestionAnswerEntity answer = new QuestionAnswerEntity();
                    answer.setResponseId(savedResponse.getId());
                    answer.setQuestionId(answerRequest.getQuestionId());
                    answer.setAnswerValue(answerRequest.getAnswerValue());
                    answer.setAnswerText(answerRequest.getAnswerText());
                    return answer;
                })
                .collect(Collectors.toList());

        questionAnswerRepository.saveAll(answers);

        // Publish event for analytics processing
        eventPublisher.publishEvent(new SurveyResponseSubmittedEvent(savedResponse.getId()));

        return savedResponse.getId();
    }
}
