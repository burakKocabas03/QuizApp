package com.ege.quizapp.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelControllerAdvice {
    @ModelAttribute("username")
    public String username(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
