package com.example.jwtnimbus.service;

import com.example.jwtnimbus.entity.User;
import com.example.jwtnimbus.exception.EmailAlreadyExistsException;
import com.example.jwtnimbus.model.LoginRequest;
import com.example.jwtnimbus.model.RegisterRequest;
import com.example.jwtnimbus.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public User signup(RegisterRequest input) {
        String email = normalizeEmail(input.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email da duoc su dung");
        }

        User user = new User(
                input.fullName().trim(),
                email,
                blankToNull(input.images()),
                passwordEncoder.encode(input.password())
        );
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User authenticate(LoginRequest input) {
        String email = normalizeEmail(input.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, input.password())
        );
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Nguoi dung vua xac thuc khong ton tai"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
