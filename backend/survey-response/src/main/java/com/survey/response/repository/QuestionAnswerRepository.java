package com.survey.response.repository;

import com.survey.response.entity.QuestionAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswerEntity, Long> {
    List<QuestionAnswerEntity> findAllByResponseId(Long responseId);
}
