package com.example.jwtnimbus.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Ho ten khong duoc de trong")
        @Size(max = 100, message = "Ho ten toi da 100 ky tu")
        String fullName,

        @NotBlank(message = "Email khong duoc de trong")
        @Email(message = "Email khong dung dinh dang")
        String email,

        @NotBlank(message = "Mat khau khong duoc de trong")
        @Size(min = 6, max = 72, message = "Mat khau phai co tu 6 den 72 ky tu")
        String password,

        @Size(max = 500, message = "Duong dan anh toi da 500 ky tu")
        String images
) {
}
