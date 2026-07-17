package com.team1.__spring_team1.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProjectJoinRequest(
        @NotBlank(message = "초대 토큰은 필수입니다.")
        String inviteToken
) {
}