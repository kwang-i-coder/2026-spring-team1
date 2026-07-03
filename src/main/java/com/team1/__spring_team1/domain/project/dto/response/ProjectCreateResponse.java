package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStage;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;

import java.time.LocalDate;

public record ProjectCreateResponse(
        Long projectId,
        String title,
        String description,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status,
        ProjectStage currentStage,
        ProjectMemberRole role
) {

    public static ProjectCreateResponse from(Project project, ProjectMemberRole role) {
        return new ProjectCreateResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getGoal(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getCurrentStage(),
                role
        );
    }
}