package com.ege.quizapp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank
    @Size(min = 5, max = 60)
    private String password;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
