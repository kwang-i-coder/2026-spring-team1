package com.team1.__spring_team1.domain.wireframe.dto.response;

public record WireframeRegenerationCreateResponse(
        Long requestId,
        Long screenId,
        String status,
        String message
) {
}
