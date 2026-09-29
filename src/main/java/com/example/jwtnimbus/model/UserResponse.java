package com.example.jwtnimbus.model;

import com.example.jwtnimbus.entity.User;
import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String images,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getImages(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
