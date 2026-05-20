package com.ege.quizapp.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ege.quizapp.model.AppUser;
import com.ege.quizapp.model.Attempt;
import com.ege.quizapp.model.AttemptAnswer;
import com.ege.quizapp.model.Question;
import com.ege.quizapp.model.Quiz;
import com.ege.quizapp.repository.AttemptRepository;
import com.ege.quizapp.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuizService {
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;

    public QuizService(QuestionRepository questionRepository, AttemptRepository attemptRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    public List<Question> questionsFor(Quiz quiz) {
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            return quiz.getQuestions().stream()
                    .filter(Question::isActive)
                    .limit(quiz.getQuestionCount())
                    .collect(Collectors.toList());
        }

        List<Question> allQuestions = questionRepository.findByCategoryAndActiveTrueOrderByIdAsc(quiz.getCategory());
        Collections.shuffle(allQuestions);
        return allQuestions.stream()
                .limit(quiz.getQuestionCount())
                .collect(Collectors.toList());
    }

    public Attempt grade(Quiz quiz, AppUser user, List<Question> questions,
                         Map<Long, String> selectedAnswers, int secondsSpent) {
        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setQuizTitle(quiz.getTitle());
        attempt.setCategoryName(quiz.getCategory().getName());
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setTotalQuestions(questions.size());
        attempt.setSecondsSpent(secondsSpent);

        int correctCount = 0;
        for (Question question : questions) {
            String selected = selectedAnswers.getOrDefault(question.getId(), "-");
            boolean correct = question.getCorrectOption().equalsIgnoreCase(selected);
            if (correct) {
                correctCount++;
            }
            attempt.addAnswer(new AttemptAnswer(question.getPrompt(), selected, question.getCorrectOption(), correct));
        }

        attempt.setCorrectAnswers(correctCount);
        attempt.setScore(questions.isEmpty() ? 0 : Math.round((correctCount * 100.0f) / questions.size()));
        return attemptRepository.save(attempt);
    }
}
