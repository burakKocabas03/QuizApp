# Online Quiz Application

Spring Boot MVC project for the Back-End Software Development assignment.

## Run

```bash
mvn spring-boot:run
```

Open: http://localhost:8080/login

## Demo Accounts

- Admin: `admin` / `admin123`
- User: `user` / `user123`

## Run With PostgreSQL

The default database is H2: `jdbc:h2:file:./data/quizdb`.

To use PostgreSQL, create a database in Postgres.app named `quizlab_db`, then run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Default PostgreSQL settings:

- URL: `jdbc:postgresql://localhost:5432/quizlab_db`
- Username: `postgres`
- Password: empty

If your Postgres.app user or password is different:

```bash
DB_URL=jdbc:postgresql://localhost:5432/quizlab_db DB_USERNAME=your_user DB_PASSWORD=your_password mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Spring/JPA creates the tables automatically because `spring.jpa.hibernate.ddl-auto=update`.

## Features

- User registration and login
- User/admin roles with Spring Security
- Quiz solving with countdown timer
- Score and past result pages
- Canvas-based result chart
- Leaderboard
- Admin question add/edit/delete
- Admin category-based quiz creation
- Admin user result view
- H2 database connection with JPA entities and repositories
- Integrated Lovable/QuizLab UI bundle under Thymeleaf templates and `/static/css/styles.css`

The H2 console is available at `/h2-console` while the app is running with the default H2 profile.

Database URL: `jdbc:h2:file:./data/quizdb`
