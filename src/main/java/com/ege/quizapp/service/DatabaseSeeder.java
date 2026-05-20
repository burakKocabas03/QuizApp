package com.ege.quizapp.service;

import com.ege.quizapp.model.AppUser;
import com.ege.quizapp.model.Category;
import com.ege.quizapp.model.Question;
import com.ege.quizapp.model.Quiz;
import com.ege.quizapp.model.Role;
import com.ege.quizapp.repository.AppUserRepository;
import com.ege.quizapp.repository.CategoryRepository;
import com.ege.quizapp.repository.QuestionRepository;
import com.ege.quizapp.repository.QuizRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private final AppUserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(AppUserRepository userRepository, CategoryRepository categoryRepository,
                          QuestionRepository questionRepository, QuizRepository quizRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedQuizData();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new AppUser("System Admin", "admin",
                    passwordEncoder.encode("admin123"), Role.ADMIN));
        }
        if (!userRepository.existsByUsername("user")) {
            userRepository.save(new AppUser("Demo User", "user",
                    passwordEncoder.encode("user123"), Role.USER));
        }
    }

    private void seedQuizData() {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category backend = categoryRepository.save(new Category("Backend Development"));
        Category database = categoryRepository.save(new Category("Database Systems"));
        Category web = categoryRepository.save(new Category("Web Fundamentals"));

        questionRepository.save(new Question("Which annotation starts a Spring Boot application?",
                "@SpringBootApplication", "@EnableJpa", "@ControllerAdvice", "@RepositoryRestResource",
                "A", backend));
        questionRepository.save(new Question("Which layer usually contains business rules in Spring MVC?",
                "Template", "Service", "Static asset", "Migration", "B", backend));
        questionRepository.save(new Question("What does JPA primarily help with?",
                "CSS compilation", "Object-relational mapping", "Password hashing", "HTTP routing", "B", backend));
        questionRepository.save(new Question("Which HTTP method is most suitable for creating a new resource?",
                "GET", "TRACE", "POST", "OPTIONS", "C", backend));

        questionRepository.save(new Question("Which SQL command reads rows from a table?",
                "SELECT", "PUSH", "READFILE", "BIND", "A", database));
        questionRepository.save(new Question("A foreign key is used to...",
                "Compress columns", "Link records between tables", "Encrypt passwords", "Create HTML forms",
                "B", database));
        questionRepository.save(new Question("Which database is embedded and useful for Spring demos?",
                "H2", "S3", "SMTP", "NPM", "A", database));
        questionRepository.save(new Question("What does ACID describe?",
                "Transaction reliability", "CSS colors", "API naming", "Image formats", "A", database));

        questionRepository.save(new Question("Which technology is responsible for page structure?",
                "HTML", "SQL", "JWT", "JPA", "A", web));
        questionRepository.save(new Question("Which status code usually means unauthorized?",
                "200", "401", "302", "500", "B", web));
        questionRepository.save(new Question("Which browser feature stores small key-value data?",
                "localStorage", "Servlet", "Bean", "Repository", "A", web));
        questionRepository.save(new Question("What is the purpose of CSRF protection?",
                "Prevent unwanted cross-site form submissions", "Improve image compression", "Sort database rows",
                "Compile Java code", "A", web));

        quizRepository.save(new Quiz("Spring Backend Basics", backend, 300, 4));
        quizRepository.save(new Quiz("Database Quick Check", database, 240, 4));
        quizRepository.save(new Quiz("Web Essentials", web, 240, 4));
    }
}
