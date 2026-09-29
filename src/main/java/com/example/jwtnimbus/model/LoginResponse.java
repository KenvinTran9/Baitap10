package com.example.jwtnimbus.model;

public record LoginResponse(String token, String tokenType, long expiresIn) {
}
