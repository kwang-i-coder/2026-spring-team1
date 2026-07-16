package com.team1.__spring_team1.domain.auth.controller;

import com.team1.__spring_team1.domain.auth.dto.SignupRequest;
import com.team1.__spring_team1.domain.auth.dto.SignupResponse;
import com.team1.__spring_team1.domain.auth.service.AuthService;
import com.team1.__spring_team1.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}