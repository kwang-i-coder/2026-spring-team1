package com.team1.__spring_team1.domain.project.dto.response;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;

import java.time.LocalDate;

public record ProjectDetailResponse(
        Long projectId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String goal,
        ProjectMemberRole myRole,
        ProjectStatus status
) {

    public static ProjectDetailResponse of(Project project, ProjectMemberRole myRole) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getGoal(),
                myRole,
                project.getStatus()
        );
    }
}