package com.team1.__spring_team1.domain.wireframe.dto.response;

import java.util.List;

public record WireframeDslResponse(
        String type,
        int width,
        int height,
        List<WireframeElementResponse> elements
) {
}
