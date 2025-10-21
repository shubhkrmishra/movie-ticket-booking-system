package com.sk.movie.services;

import com.sk.movie.dto.AuthResponse;
import com.sk.movie.dto.LoginRequest;
import com.sk.movie.dto.RegisterRequest;
import com.sk.movie.entities.User;
import com.sk.movie.entities.UserRole;
import com.sk.movie.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Username already taken");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ?
                UserRole.valueOf(request.getRole()) : UserRole.CUSTOMER);
        userRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setToken("demo-jwt-token-for-" + user.getUsername());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        responseValidation(user, request.getPassword());
        AuthResponse res = new AuthResponse();
        res.setUserId(user.getUserId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole().name());
        res.setToken("demo-jwt-token-for-" + user.getUsername());
        return res;
    }

    private void responseValidation(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash()))
            throw new RuntimeException("Incorrect password");
    }
}
