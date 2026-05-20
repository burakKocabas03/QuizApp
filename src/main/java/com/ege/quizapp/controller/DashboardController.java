package com.ege.quizapp.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ege.quizapp.model.AppUser;
import com.ege.quizapp.model.Attempt;
import com.ege.quizapp.model.Question;
import com.ege.quizapp.model.Quiz;
import com.ege.quizapp.model.Role;
import com.ege.quizapp.repository.AttemptRepository;
import com.ege.quizapp.repository.QuizRepository;
import com.ege.quizapp.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;
    private final UserService userService;

    public DashboardController(QuizRepository quizRepository, AttemptRepository attemptRepository,
                               UserService userService) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.userService = userService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        AppUser user = userService.currentUser(authentication.getName());
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin";
        }

        model.addAttribute("user", user);
        List<Attempt> attempts = attemptRepository.findByUserOrderBySubmittedAtDesc(user);
        model.addAttribute("quizzes", quizRepository.findByActiveTrueOrderByTitleAsc().stream()
                .map(this::quizSummary)
                .collect(Collectors.toList()));
        model.addAttribute("results", attempts.stream()
                .map(this::resultSummary)
                .collect(Collectors.toList()));
        model.addAttribute("attempts", attempts);
        model.addAttribute("leaderboard", leaderboardEntries());
        return "dashboard";
    }

    private Map<String, Object> quizSummary(Quiz quiz) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", quiz.getId());
        item.put("title", quiz.getTitle());
        item.put("description", "A focused " + quiz.getCategory().getName() + " quiz generated from database questions.");
        item.put("category", quiz.getCategory().getName());
        item.put("questionCount", visibleQuestionCount(quiz));
        item.put("timeLimit", Math.max(1, quiz.getTimeLimitSeconds() / 60));
        item.put("attempts", null);
        return item;
    }

    private int visibleQuestionCount(Quiz quiz) {
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            return (int) quiz.getQuestions().stream()
                    .filter(Question::isActive)
                    .count();
        }
        return quiz.getQuestionCount();
    }

    private Map<String, Object> resultSummary(Attempt attempt) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", attempt.getId());
        item.put("quizTitle", attempt.getQuizTitle());
        item.put("category", attempt.getCategoryName());
        item.put("score", attempt.getCorrectAnswers());
        item.put("total", attempt.getTotalQuestions());
        item.put("takenAt", attempt.getSubmittedAt());
        return item;
    }

    private List<Map<String, Object>> leaderboardEntries() {
        Map<Long, Map<String, Object>> totalsByUser = new HashMap<>();

        for (Attempt attempt : attemptRepository.findAllByOrderBySubmittedAtDesc()) {
            AppUser attemptUser = attempt.getUser();
            Map<String, Object> entry = totalsByUser.computeIfAbsent(attemptUser.getId(), userId -> {
                Map<String, Object> newEntry = new HashMap<>();
                newEntry.put("username", attemptUser.getFullName());
                newEntry.put("totalScore", 0);
                newEntry.put("attemptCount", 0);
                return newEntry;
            });

            entry.put("totalScore", (Integer) entry.get("totalScore") + attempt.getScore());
            entry.put("attemptCount", (Integer) entry.get("attemptCount") + 1);
        }

        return totalsByUser.values().stream()
                .sorted(Comparator.comparingInt(entry -> -((Integer) entry.get("totalScore"))))
                .limit(10)
                .collect(Collectors.toList());
    }
}
