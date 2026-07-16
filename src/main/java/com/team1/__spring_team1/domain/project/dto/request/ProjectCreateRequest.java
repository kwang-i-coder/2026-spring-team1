package com.team1.__spring_team1.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectCreateRequest(
        @NotBlank(message = "프로젝트 제목은 필수입니다.")
        @Size(max = 100, message = "프로젝트 제목은 100자 이하여야야 합니다.")
        String title,
        String description,
        String goal,
        LocalDate startDate,
        LocalDate endDate
) {
}