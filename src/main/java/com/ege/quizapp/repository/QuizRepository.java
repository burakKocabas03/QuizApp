package com.ege.quizapp.repository;

import java.util.List;
import com.ege.quizapp.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByActiveTrueOrderByTitleAsc();
}
