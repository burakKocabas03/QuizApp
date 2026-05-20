# QuizLab — Frontend Bundle (Spring Boot + Thymeleaf)

Drop-in HTML + CSS + JS for a Spring Boot MVC quiz application.

## Install

Copy the files into your Spring Boot project:

```
quizlab/templates/   →  src/main/resources/templates/
quizlab/static/      →  src/main/resources/static/
```

Make sure Thymeleaf is on the classpath (`spring-boot-starter-thymeleaf`).

## File map

| Template                          | Route                                  | Controller method |
|-----------------------------------|----------------------------------------|-------------------|
| `templates/login.html`            | `GET /login`                           | render login      |
| `templates/register.html`         | `GET /register`, `POST /register`      | model `user`      |
| `templates/dashboard.html`        | `GET /dashboard`                       | model `user`, `quizzes`, `results`, `leaderboard` |
| `templates/quiz.html`             | `GET /quiz/{id}`                       | model `quiz` (with `questions[].options[]`) |
| `templates/result.html`           | `GET /result/{id}`                     | model `result` (`score`, `total`, `reviews[]`) |
| `templates/admin/dashboard.html`  | `GET /admin`                           | model `quizzes`, `questions`, `userResults`, `categories` |
| `templates/admin/question-form.html` | `GET /admin/questions/new`, `GET /admin/questions/{id}/edit` | model `question`, `categories` |
| `fragments/layout.html`           | shared `<head>` + top bar              | uses `session.username` |

## Form posts (already wired)

- `POST /login`                        — `username`, `password`
- `POST /register`                     — `fullName`, `username`, `password`
- `POST /logout`
- `POST /quiz/{id}/submit`             — `answers[<questionId>]=<optionIndex>`
- `POST /admin/questions`              — create (`text`, `optionA`..`optionD`, `correctOption`, `category`)
- `POST /admin/questions/{id}`         — update (same fields)
- `POST /admin/questions/{id}/delete`  — delete
- `POST /admin/quizzes`                — `title`, `category`, `timeLimit`, `questionCount`
- `POST /admin/quizzes/{id}/delete`

## Expected model shapes (suggested)

```java
record QuizSummary(Long id, String title, String description, String category,
                   int questionCount, int timeLimit, Integer attempts) {}

record QuizDetail(Long id, String title, String category, int timeLimit,
                  List<Question> questions) {}

record Question(Long id, String text, List<String> options, int correctOption, String category) {}

record ResultSummary(Long id, String quizTitle, String category, int score, int total,
                     LocalDateTime takenAt) {}

record ResultDetail(String quizTitle, String category, int score, int total,
                    int correctCount, int wrongCount, String timeTaken,
                    LocalDateTime takenAt, List<Review> reviews) {}

record Review(String questionText, String selectedAnswer, String correctAnswer, boolean correct) {}

record LeaderboardEntry(String username, int totalScore) {}

record AdminResult(String username, String quizTitle, int score, int total, LocalDateTime takenAt) {}
```

The templates are defensive against `null` collections and render empty states.

## Notes

- Light theme, Inter font (loaded from Google Fonts).
- Countdown timer (`static/js/quiz.js`) auto-submits the quiz at 0:00 and turns
  orange under 1 min, red under 20s.
- Tables scroll horizontally on small screens.
- All routes use Thymeleaf `@{...}` URL syntax so a context path (e.g. `/quizlab`)
  works automatically.
- For Spring Security with CSRF enabled, add the following inside each `<form>`:
  ```html
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
  ```
