package com.ege.quizapp.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.ege.quizapp.model.Category;
import com.ege.quizapp.model.Attempt;
import com.ege.quizapp.model.Question;
import com.ege.quizapp.model.Quiz;
import com.ege.quizapp.repository.AttemptRepository;
import com.ege.quizapp.repository.CategoryRepository;
import com.ege.quizapp.repository.QuestionRepository;
import com.ege.quizapp.repository.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AdminController {
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;

    public AdminController(CategoryRepository categoryRepository, QuestionRepository questionRepository,
                           QuizRepository quizRepository, AttemptRepository attemptRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("questions", questionRepository.findByActiveTrueOrderByIdDesc().stream()
                .map(this::questionRow)
                .collect(Collectors.toList()));
        model.addAttribute("quizzes", quizRepository.findByActiveTrueOrderByTitleAsc().stream()
                .map(this::quizRow)
                .collect(Collectors.toList()));
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("userResults", attemptRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(this::resultRow)
                .collect(Collectors.toList()));
        model.addAttribute("attempts", attemptRepository.findAllByOrderBySubmittedAtDesc());
        return "admin/dashboard";
    }

    @GetMapping("/admin/questions/new")
    public String newQuestion(Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("mode", "create");
        return "admin/question-form";
    }

    @GetMapping("/admin/questions/{id}/edit")
    public String editQuestion(@PathVariable Long id, Model model) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("question", question);
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("mode", "edit");
        return "admin/question-form";
    }

    @GetMapping("/admin/quizzes/{id}/edit")
    public String editQuiz(@PathVariable Long id, Model model) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<Question> selectedQuestions = quizQuestions(quiz);
        Set<Long> selectedQuestionIds = selectedQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        model.addAttribute("quiz", quiz);
        model.addAttribute("selectedQuestions", selectedQuestions);
        model.addAttribute("selectedQuestionIds", selectedQuestionIds);
        model.addAttribute("availableQuestions", questionRepository.findByActiveTrueOrderByIdDesc());
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        return "admin/quiz-form";
    }

    @PostMapping("/admin/questions")
    public String createQuestion(@RequestParam String prompt,
                                 @RequestParam String optionA,
                                 @RequestParam String optionB,
                                 @RequestParam String optionC,
                                 @RequestParam String optionD,
                                 @RequestParam String correctOption,
                                 @RequestParam Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        questionRepository.save(new Question(prompt, optionA, optionB, optionC, optionD,
                correctOption.toUpperCase(), category));
        return "redirect:/admin";
    }

    @PostMapping("/admin/questions/{id}")
    public String updateQuestion(@PathVariable Long id,
                                 @RequestParam String prompt,
                                 @RequestParam String optionA,
                                 @RequestParam String optionB,
                                 @RequestParam String optionC,
                                 @RequestParam String optionD,
                                 @RequestParam String correctOption,
                                 @RequestParam Long categoryId) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        question.setPrompt(prompt);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectOption(correctOption.toUpperCase());
        question.setCategory(category);
        questionRepository.save(question);
        return "redirect:/admin";
    }

    @PostMapping("/admin/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        question.setActive(false);
        questionRepository.save(question);
        return "redirect:/admin";
    }

    @PostMapping("/admin/quizzes")
    public String createQuiz(@RequestParam String title,
                             @RequestParam Long categoryId,
                             @RequestParam int timeLimit,
                             @RequestParam int questionCount) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Quiz quiz = new Quiz(title, category, timeLimit * 60, questionCount);
        quiz.setQuestions(questionRepository.findByCategoryAndActiveTrueOrderByIdAsc(category).stream()
                .limit(questionCount)
                .collect(Collectors.toList()));
        quiz.setQuestionCount(quiz.getQuestions().isEmpty() ? questionCount : quiz.getQuestions().size());
        quizRepository.save(quiz);
        return "redirect:/admin";
    }

    @PostMapping("/admin/quizzes/{id}")
    public String updateQuiz(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam Long categoryId,
                             @RequestParam int timeLimit,
                             @RequestParam(required = false) List<Long> questionIds) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        List<Question> selectedQuestions = questionIds == null
                ? Collections.emptyList()
                : questionRepository.findAllById(questionIds).stream()
                        .filter(Question::isActive)
                        .collect(Collectors.toList());

        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setTimeLimitSeconds(timeLimit * 60);
        quiz.setQuestions(selectedQuestions);
        quiz.setQuestionCount(selectedQuestions.size());
        quizRepository.save(quiz);
        return "redirect:/admin/quizzes/" + id + "/edit?saved";
    }

    @PostMapping("/admin/quizzes/{id}/questions")
    public String createQuestionForQuiz(@PathVariable Long id,
                                        @RequestParam String prompt,
                                        @RequestParam String optionA,
                                        @RequestParam String optionB,
                                        @RequestParam String optionC,
                                        @RequestParam String optionD,
                                        @RequestParam String correctOption,
                                        @RequestParam Long categoryId) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Question question = questionRepository.save(new Question(prompt, optionA, optionB, optionC, optionD,
                correctOption.toUpperCase(), category));

        List<Question> quizQuestions = quizQuestions(quiz);
        quizQuestions.add(question);
        quiz.setQuestions(quizQuestions);
        quiz.setCategory(category);
        quiz.setQuestionCount(quiz.getQuestions().size());
        quizRepository.save(quiz);
        return "redirect:/admin/quizzes/" + id + "/edit?questionAdded";
    }

    private Map<String, Object> questionRow(Question question) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", question.getId());
        item.put("text", question.getPrompt());
        item.put("category", question.getCategory().getName());
        item.put("correctOption", "ABCD".indexOf(question.getCorrectOption()));
        item.put("correctLetter", question.getCorrectOption());
        return item;
    }

    private Map<String, Object> quizRow(Quiz quiz) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", quiz.getId());
        item.put("title", quiz.getTitle());
        item.put("category", quiz.getCategory().getName());
        item.put("questionCount", visibleQuestionCount(quiz));
        item.put("timeLimit", Math.max(1, quiz.getTimeLimitSeconds() / 60));
        return item;
    }

    private List<Question> quizQuestions(Quiz quiz) {
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            return quiz.getQuestions().stream()
                    .filter(Question::isActive)
                    .collect(Collectors.toList());
        }
        return questionRepository.findByCategoryAndActiveTrueOrderByIdAsc(quiz.getCategory()).stream()
                .limit(quiz.getQuestionCount())
                .collect(Collectors.toList());
    }

    private int visibleQuestionCount(Quiz quiz) {
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            return (int) quiz.getQuestions().stream()
                    .filter(Question::isActive)
                    .count();
        }
        return quiz.getQuestionCount();
    }

    private Map<String, Object> resultRow(Attempt attempt) {
        Map<String, Object> item = new HashMap<>();
        item.put("username", attempt.getUser().getFullName());
        item.put("quizTitle", attempt.getQuizTitle());
        item.put("score", attempt.getCorrectAnswers());
        item.put("total", attempt.getTotalQuestions());
        item.put("takenAt", attempt.getSubmittedAt());
        return item;
    }

    @PostMapping("/admin/quizzes/{id}/delete")
    public String deleteQuiz(@PathVariable Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        quiz.setActive(false);
        quizRepository.save(quiz);
        return "redirect:/admin";
    }
}
