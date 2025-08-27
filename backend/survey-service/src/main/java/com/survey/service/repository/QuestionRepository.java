package com.survey.service.repository;

import com.survey.service.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findBySurveyIdOrderByOrderIndex(UUID surveyId);

    int countBySurveyId(UUID surveyId);
}
