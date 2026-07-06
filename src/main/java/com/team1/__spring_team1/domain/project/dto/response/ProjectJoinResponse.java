package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;

public record ProjectJoinResponse(
        Long projectId,
        String title,
        ProjectMemberRole role
) {

    public static ProjectJoinResponse of(Project project, ProjectMemberRole role) {
        return new ProjectJoinResponse(
                project.getId(),
                project.getTitle(),
                role
        );
    }
}