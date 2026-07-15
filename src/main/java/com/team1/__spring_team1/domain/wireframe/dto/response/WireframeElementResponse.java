package com.team1.__spring_team1.domain.wireframe.dto.response;

public record WireframeElementResponse(
        String id,
        String type,
        String text,
        int x,
        int y,
        int w,
        int h
) {
}
