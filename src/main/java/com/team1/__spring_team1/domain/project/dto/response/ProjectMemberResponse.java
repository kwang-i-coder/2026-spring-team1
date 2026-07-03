package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.ProjectMember;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;

import java.time.LocalDateTime;

public record ProjectMemberResponse(
        Long projectMemberId,
        Long userId,
        ProjectMemberRole role,
        LocalDateTime joinedAt
) {

    public static ProjectMemberResponse from(ProjectMember projectMember) {
        return new ProjectMemberResponse(
                projectMember.getId(),
                projectMember.getUserId(),
                projectMember.getRole(),
                projectMember.getJoinedAt()
        );
    }
}