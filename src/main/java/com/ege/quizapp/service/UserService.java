package com.ege.quizapp.service;

import com.ege.quizapp.dto.RegisterRequest;
import com.ege.quizapp.model.AppUser;
import com.ege.quizapp.model.Role;
import com.ege.quizapp.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(RegisterRequest request) {
        AppUser user = new AppUser(request.getFullName(), request.getUsername(),
                passwordEncoder.encode(request.getPassword()), Role.USER);
        return userRepository.save(user);
    }

    public AppUser currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user could not be found."));
    }
}
