package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;

public record ProjectJoinResponse(
        Long projectId,
        Long userId,
        ProjectMemberRole role
) {
}