package com.team1.__spring_team1.global.security;

public record LoginUser(
        Long userId,
        String loginId,
        String name
) {
}