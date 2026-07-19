package com.team1.__spring_team1.domain.auth.dto;

import com.team1.__spring_team1.global.security.LoginUser;

public record MeResponse(
        Long userId,
        String loginId,
        String name
) {

    public static MeResponse from(LoginUser loginUser) {
        return new MeResponse(
                loginUser.userId(),
                loginUser.loginId(),
                loginUser.name()
        );
    }
}