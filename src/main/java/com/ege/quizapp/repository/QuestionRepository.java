package com.ege.quizapp.repository;

import java.util.List;
import com.ege.quizapp.model.Category;
import com.ege.quizapp.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByActiveTrueOrderByIdDesc();
    List<Question> findByCategoryAndActiveTrueOrderByIdAsc(Category category);
}
