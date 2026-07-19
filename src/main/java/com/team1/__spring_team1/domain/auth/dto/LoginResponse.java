package com.team1.__spring_team1.domain.auth.dto;

import com.team1.__spring_team1.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String loginId;
    private String name;
    private String sessionToken;

    public static LoginResponse from(User user, String sessionToken) {
        return new LoginResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                sessionToken
        );
    }
}