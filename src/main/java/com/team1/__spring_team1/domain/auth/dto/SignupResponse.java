package com.team1.__spring_team1.domain.auth.dto;

import com.team1.__spring_team1.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private Long id;
    private String loginId;
    private String name;

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getLoginId(),
                user.getName()
        );
    }
}