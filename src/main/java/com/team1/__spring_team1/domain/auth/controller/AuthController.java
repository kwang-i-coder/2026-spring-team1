package com.team1.__spring_team1.domain.auth.controller;

import com.team1.__spring_team1.domain.auth.dto.LoginRequest;
import com.team1.__spring_team1.domain.auth.dto.LoginResponse;
import com.team1.__spring_team1.domain.auth.dto.LoginResult;
import com.team1.__spring_team1.domain.auth.dto.MeResponse;
import com.team1.__spring_team1.domain.auth.dto.SignupRequest;
import com.team1.__spring_team1.domain.auth.dto.SignupResponse;
import com.team1.__spring_team1.domain.auth.service.AuthService;
import com.team1.__spring_team1.global.response.ApiResponse;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String SESSION_COOKIE_NAME = "SESSION";

    private final AuthService authService;

    @Value("${app.auth.cookie.secure}")
    private boolean sessionCookieSecure;

    @Value("${app.auth.cookie.same-site}")
    private String sessionCookieSameSite;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);

        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResult result = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from(
                        SESSION_COOKIE_NAME,
                        result.sessionToken()
                )
                .httpOnly(true)
                .secure(sessionCookieSecure)
                .sameSite(sessionCookieSameSite)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(result.response()));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(
            @Parameter(hidden = true)
            @CurrentUser LoginUser loginUser
    ) {
        return ApiResponse.success(MeResponse.from(loginUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true)
            @CookieValue(name = SESSION_COOKIE_NAME) String sessionToken
    ) {
        authService.logout(sessionToken);

        ResponseCookie expiredCookie = ResponseCookie.from(
                        SESSION_COOKIE_NAME,
                        ""
                )
                .httpOnly(true)
                .secure(sessionCookieSecure)
                .sameSite(sessionCookieSameSite)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(ApiResponse.success(null));
    }
}