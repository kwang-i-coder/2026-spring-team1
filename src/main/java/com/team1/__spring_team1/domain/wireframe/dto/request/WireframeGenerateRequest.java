package com.team1.__spring_team1.domain.wireframe.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WireframeGenerateRequest(
        @NotEmpty(message="생성할 화면을 하나 이상 선택해주셔야 합니다.")
        List<Long> screenIds
) {
}
