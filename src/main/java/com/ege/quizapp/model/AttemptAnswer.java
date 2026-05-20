package com.ege.quizapp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class AttemptAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Attempt attempt;

    @Column(length = 1000)
    private String questionText;

    private String selectedOption;
    private String correctOption;
    private boolean correct;

    public AttemptAnswer() {
    }

    public AttemptAnswer(String questionText, String selectedOption, String correctOption, boolean correct) {
        this.questionText = questionText;
        this.selectedOption = selectedOption;
        this.correctOption = correctOption;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public void setAttempt(Attempt attempt) {
        this.attempt = attempt;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public boolean isCorrect() {
        return correct;
    }
}
