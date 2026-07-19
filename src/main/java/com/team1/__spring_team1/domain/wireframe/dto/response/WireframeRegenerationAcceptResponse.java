package com.team1.__spring_team1.domain.wireframe.dto.response;

public record WireframeRegenerationAcceptResponse(
        Long requestId,
        Long screenId,
        String status,
        String message,
        WireframeDslResponse wireframe
) {
}
