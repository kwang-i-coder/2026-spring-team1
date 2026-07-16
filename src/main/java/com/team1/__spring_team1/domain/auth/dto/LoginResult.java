package com.team1.__spring_team1.domain.auth.dto;

public record LoginResult(
        LoginResponse response,
        String sessionToken
) {
}