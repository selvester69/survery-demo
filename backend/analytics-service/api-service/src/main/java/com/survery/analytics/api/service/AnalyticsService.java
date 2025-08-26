package com.survery.analytics.api.service;

import com.survery.analytics.api.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SurveyRepository surveyRepository;

    @Transactional(readOnly = true)
    public long getSurveyCount() {
        return surveyRepository.count();
    }
}
