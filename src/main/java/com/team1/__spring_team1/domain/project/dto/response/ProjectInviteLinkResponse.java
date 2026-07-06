package com.team1.__spring_team1.domain.project.dto.response;

public record ProjectInviteLinkResponse(
        String inviteToken,
        String inviteUrl
) {
}