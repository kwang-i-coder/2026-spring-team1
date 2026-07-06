package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;

public record ProjectListItemResponse(
        Long projectId,
        String title,
        String description,
        ProjectMemberRole role,
        ProjectStatus status
) {

    public static ProjectListItemResponse of(Project project, ProjectMemberRole role) {
        return new ProjectListItemResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                role,
                project.getStatus()
        );
    }
}