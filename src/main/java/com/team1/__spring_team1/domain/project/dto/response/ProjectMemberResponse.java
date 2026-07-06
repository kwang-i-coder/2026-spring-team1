package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.user.entity.User;

public record ProjectMemberResponse(
        Long userId,
        String name,
        String loginId,
        ProjectMemberRole role
) {

    public static ProjectMemberResponse of(User user, ProjectMemberRole role) {
        return new ProjectMemberResponse(
                user.getId(),
                user.getName(),
                user.getLoginId(),
                role
        );
    }

    public static ProjectMemberResponse ofMock(Long userId, ProjectMemberRole role) {
        return new ProjectMemberResponse(
                userId,
                "개발용 사용자 " + userId,
                "mock-user-" + userId,
                role
        );
    }
}