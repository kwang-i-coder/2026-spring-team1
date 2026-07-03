package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStage;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;

import java.time.LocalDate;

public record ProjectDetailResponse(
        Long projectId,
        String title,
        String description,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status,
        ProjectStage currentStage,
        Long createdBy,
        ProjectMemberRole myRole
) {

    public static ProjectDetailResponse of(Project project, ProjectMemberRole myRole) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getGoal(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getCurrentStage(),
                project.getCreatedBy(),
                myRole
        );
    }
}