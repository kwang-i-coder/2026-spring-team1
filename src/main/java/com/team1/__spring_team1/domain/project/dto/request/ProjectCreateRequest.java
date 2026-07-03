package com.team1.__spring_team1.domain.project.dto.request;

import java.time.LocalDate;

public record ProjectCreateRequest(
        String title,
        String description,
        String goal,
        LocalDate startDate,
        LocalDate endDate
) {
}