package com.example.jwtnimbus.controller;

import com.example.jwtnimbus.entity.User;
import com.example.jwtnimbus.model.LoginRequest;
import com.example.jwtnimbus.model.LoginResponse;
import com.example.jwtnimbus.model.RegisterRequest;
import com.example.jwtnimbus.model.UserResponse;
import com.example.jwtnimbus.service.AuthenticationService;
import com.example.jwtnimbus.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = authenticationService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(registeredUser));
    }

    @PostMapping("/login")
    public LoginResponse authenticate(@Valid @RequestBody LoginRequest request) {
        User authenticatedUser = authenticationService.authenticate(request);
        String token = jwtService.generateToken(authenticatedUser);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationTime());
    }
}
