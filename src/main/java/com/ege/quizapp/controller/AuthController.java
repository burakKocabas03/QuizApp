package com.ege.quizapp.controller;

import javax.validation.Valid;
import com.ege.quizapp.dto.RegisterRequest;
import com.ege.quizapp.repository.AppUserRepository;
import com.ege.quizapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserService userService;
    private final AppUserRepository userRepository;

    public AuthController(UserService userService, AppUserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           Model model) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "This username is already used.");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(registerRequest);
        model.addAttribute("registered", true);
        return "redirect:/login?registered";
    }
}
