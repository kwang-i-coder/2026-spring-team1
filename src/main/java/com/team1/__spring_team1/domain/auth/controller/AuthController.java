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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
                        "SESSION",
                        result.sessionToken()
                )
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(result.response()));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(
            @CurrentUser LoginUser loginUser
    ) {
        return ApiResponse.success(MeResponse.from(loginUser));
    }
}