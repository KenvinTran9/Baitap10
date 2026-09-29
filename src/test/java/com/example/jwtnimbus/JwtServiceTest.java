package com.example.jwtnimbus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.jwtnimbus.exception.JwtAuthenticationException;
import com.example.jwtnimbus.service.JwtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void nimbusCreatesAndVerifiesHs256Token() {
        User principal = new User(
                "alice@example.com",
                "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(principal);

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.extractAndValidateSubject(token)).isEqualTo("alice@example.com");
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    void rejectsTamperedToken() {
        User principal = new User(
                "alice@example.com",
                "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtService.generateToken(principal);
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThatThrownBy(() -> jwtService.extractAndValidateSubject(tampered))
                .isInstanceOf(JwtAuthenticationException.class);
    }
}
