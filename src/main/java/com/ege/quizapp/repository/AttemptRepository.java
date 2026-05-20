package com.ege.quizapp.repository;

import java.util.List;
import com.ege.quizapp.model.AppUser;
import com.ege.quizapp.model.Attempt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByUserOrderBySubmittedAtDesc(AppUser user);
    List<Attempt> findAllByOrderBySubmittedAtDesc();
    List<Attempt> findTop10ByOrderByScoreDescCorrectAnswersDescSubmittedAtAsc();

    @Override
    @EntityGraph(attributePaths = {"answers", "user"})
    java.util.Optional<Attempt> findById(Long id);
}
