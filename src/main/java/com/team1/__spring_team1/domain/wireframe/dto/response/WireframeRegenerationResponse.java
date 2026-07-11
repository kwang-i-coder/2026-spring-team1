package com.team1.__spring_team1.domain.wireframe.dto.response;

import java.time.LocalDateTime;

public record WireframeRegenerationResponse(
        Long requestId,
        Long screenId,
        String screenName,
        WireframeRegenerationRequesterResponse requestedBy,
        String reason,
        String status,
        LocalDateTime createdAt
) {
}
