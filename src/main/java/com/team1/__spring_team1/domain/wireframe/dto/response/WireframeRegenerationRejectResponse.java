package com.team1.__spring_team1.domain.wireframe.dto.response;

public record WireframeRegenerationRejectResponse(
        Long requestId,
        String status,
        String message
) {
}
