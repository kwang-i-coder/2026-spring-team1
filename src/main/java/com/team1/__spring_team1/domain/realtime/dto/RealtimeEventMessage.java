package com.team1.__spring_team1.domain.realtime.dto;

import java.time.Instant;
import java.util.Map;

public record RealtimeEventMessage(
        RealtimeEventType type,
        Long projectId,
        Long userId,
        Map<String, Object> payload,
        String timestamp
) {
    public static RealtimeEventMessage of(
            RealtimeEventType type,
            Long projectId,
            Long userId,
            Map<String, Object> payload
    ) {
        return new RealtimeEventMessage(
                type,
                projectId,
                userId,
                payload,
                Instant.now().toString()
        );
    }
}