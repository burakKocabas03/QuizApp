package com.ege.quizapp.controller;

import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ege.quizapp.model.AppUser;
import com.ege.quizapp.model.Attempt;
import com.ege.quizapp.model.Question;
import com.ege.quizapp.model.Quiz;
import com.ege.quizapp.repository.AttemptRepository;
import com.ege.quizapp.repository.QuestionRepository;
import com.ege.quizapp.repository.QuizRepository;
import com.ege.quizapp.service.QuizService;
import com.ege.quizapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class QuizController {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;
    private final QuizService quizService;
    private final UserService userService;

    public QuizController(QuizRepository quizRepository, QuestionRepository questionRepository,
                          AttemptRepository attemptRepository, QuizService quizService, UserService userService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.quizService = quizService;
        this.userService = userService;
    }

    @GetMapping("/quiz/{id}")
    public String takeQuiz(@PathVariable Long id, Model model) {
        Quiz quiz = activeQuiz(id);
        List<Question> questions = quizService.questionsFor(quiz);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This quiz has no active questions.");
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("quizView", quizDetail(quiz, questions));
        model.addAttribute("startedAt", System.currentTimeMillis());
        return "quiz";
    }

    @PostMapping("/quiz/{id}/submit")
    public String submitQuiz(@PathVariable Long id,
                             @RequestParam("questionIds") List<Long> questionIds,
                             @RequestParam("startedAt") long startedAt,
                             @RequestParam Map<String, String> params,
                             Authentication authentication) {
        Quiz quiz = activeQuiz(id);
        AppUser user = userService.currentUser(authentication.getName());
        List<Question> questions = questionIds.stream()
                .map(questionId -> questionRepository.findById(questionId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST)))
                .collect(Collectors.toList());

        Map<Long, String> selectedAnswers = new HashMap<>();
        for (Long questionId : questionIds) {
            selectedAnswers.put(questionId, params.getOrDefault("answer_" + questionId,
                    params.getOrDefault("answers[" + questionId + "]", "-")));
        }

        long elapsedMillis = Math.max(0, System.currentTimeMillis() - startedAt);
        int secondsSpent = (int) Math.min(Integer.MAX_VALUE, elapsedMillis / 1000);
        Attempt attempt = quizService.grade(quiz, user, questions, selectedAnswers, secondsSpent);
        return "redirect:/result/" + attempt.getId();
    }

    @GetMapping("/result/{id}")
    public String result(@PathVariable Long id, Authentication authentication, Model model) {
        Attempt attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!admin && !attempt.getUser().getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("attempt", attempt);
        model.addAttribute("result", resultDetail(attempt));
        return "result";
    }

    private Quiz activeQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!quiz.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return quiz;
    }

    private Map<String, Object> quizDetail(Quiz quiz, List<Question> questions) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", quiz.getId());
        item.put("title", quiz.getTitle());
        item.put("category", quiz.getCategory().getName());
        item.put("timeLimit", Math.max(1, quiz.getTimeLimitSeconds() / 60));
        item.put("timeLimitSeconds", quiz.getTimeLimitSeconds());
        item.put("questions", questions.stream()
                .map(this::questionDetail)
                .collect(Collectors.toList()));
        return item;
    }

    private Map<String, Object> questionDetail(Question question) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", question.getId());
        item.put("text", question.getPrompt());
        item.put("options", Arrays.asList(
                option("A", question.getOptionA()),
                option("B", question.getOptionB()),
                option("C", question.getOptionC()),
                option("D", question.getOptionD())));
        return item;
    }

    private Map<String, Object> option(String letter, String text) {
        Map<String, Object> item = new HashMap<>();
        item.put("letter", letter);
        item.put("text", text);
        return item;
    }

    private Map<String, Object> resultDetail(Attempt attempt) {
        Map<String, Object> item = new HashMap<>();
        item.put("quizTitle", attempt.getQuizTitle());
        item.put("category", attempt.getCategoryName());
        item.put("score", attempt.getCorrectAnswers());
        item.put("total", attempt.getTotalQuestions());
        item.put("correctCount", attempt.getCorrectAnswers());
        item.put("wrongCount", attempt.getTotalQuestions() - attempt.getCorrectAnswers());
        item.put("timeTaken", formatSeconds(attempt.getSecondsSpent()));
        item.put("takenAt", attempt.getSubmittedAt());
        item.put("reviews", attempt.getAnswers().stream().map(answer -> {
            Map<String, Object> review = new HashMap<>();
            review.put("questionText", answer.getQuestionText());
            review.put("selectedAnswer", answer.getSelectedOption());
            review.put("correctAnswer", answer.getCorrectOption());
            review.put("correct", answer.isCorrect());
            return review;
        }).collect(Collectors.toList()));
        return item;
    }

    private String formatSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
