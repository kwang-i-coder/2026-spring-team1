package com.team1.__spring_team1.domain.auth.controller;

import com.team1.__spring_team1.domain.auth.dto.LoginRequest;
import com.team1.__spring_team1.domain.auth.dto.LoginResponse;
import com.team1.__spring_team1.domain.auth.dto.LoginResult;
import com.team1.__spring_team1.domain.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerCookieTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
        ReflectionTestUtils.setField(authController, "sessionCookieSecure", true);
        ReflectionTestUtils.setField(authController, "sessionCookieSameSite", "Lax");
    }

    @Test
    void loginUsesSecureSameSiteCookieSettings() {
        LoginRequest request = mock(LoginRequest.class);
        LoginResult result = new LoginResult(
                new LoginResponse(1L, "login-id", "name"),
                "session-token"
        );
        when(authService.login(request)).thenReturn(result);

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("HttpOnly", "Secure", "SameSite=Lax");
    }

    @Test
    void logoutUsesSecureSameSiteCookieSettings() {
        ResponseEntity<?> response = authController.logout("session-token");

        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("Max-Age=0", "HttpOnly", "Secure", "SameSite=Lax");
    }
}
