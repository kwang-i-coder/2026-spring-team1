package com.team1.__spring_team1.domain.project.dto.response;

import java.time.LocalDateTime;

public record ProjectInviteLinkResponse(
        Long projectId,
        String inviteToken,
        LocalDateTime expiresAt
) {
}