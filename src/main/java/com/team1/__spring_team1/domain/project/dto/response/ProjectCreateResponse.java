package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;

public record ProjectCreateResponse(
        Long projectId,
        String title,
        ProjectMemberRole role,
        ProjectStatus status
) {

    public static ProjectCreateResponse from(Project project, ProjectMemberRole role) {
        return new ProjectCreateResponse(
                project.getId(),
                project.getTitle(),
                role,
                project.getStatus()
        );
    }
}