package com.team1.__spring_team1.domain.project.dto.response;

import java.util.List;

public record ProjectListResponse(
        List<ProjectListItemResponse> projects
) {
}