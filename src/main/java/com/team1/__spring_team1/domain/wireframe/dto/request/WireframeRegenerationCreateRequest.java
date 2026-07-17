package com.team1.__spring_team1.domain.wireframe.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WireframeRegenerationCreateRequest(
        @NotBlank(message = "재생성 요청 사유는 필수입니다.")
        String reason
) {
}
