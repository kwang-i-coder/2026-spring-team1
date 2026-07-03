package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStage;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;

import java.time.LocalDate;

public record ProjectListResponse(
        Long projectId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status,
        ProjectStage currentStage,
        ProjectMemberRole role
) {

    public static ProjectListResponse of(Project project, ProjectMemberRole role) {
        return new ProjectListResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getCurrentStage(),
                role
        );
    }
}