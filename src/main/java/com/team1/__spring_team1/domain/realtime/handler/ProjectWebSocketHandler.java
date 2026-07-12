package com.team1.__spring_team1.domain.realtime.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.realtime.dto.RealtimeEventMessage;
import com.team1.__spring_team1.domain.realtime.dto.RealtimeEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ProjectWebSocketHandler extends TextWebSocketHandler {

    private static final String PROJECT_ID_ATTRIBUTE = "projectId";
    private static final String USER_ID_ATTRIBUTE = "userId";

    private final ObjectMapper objectMapper;

    private final Map<Long, Set<WebSocketSession>> projectSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long projectId = extractProjectId(session);
        Long userId = extractUserId(session);

        session.getAttributes().put(PROJECT_ID_ATTRIBUTE, projectId);
        session.getAttributes().put(USER_ID_ATTRIBUTE, userId);

        projectSessions
                .computeIfAbsent(projectId, key -> ConcurrentHashMap.newKeySet())
                .add(session);

        RealtimeEventMessage joinMessage = RealtimeEventMessage.of(
                RealtimeEventType.USER_JOINED,
                projectId,
                userId,
                Map.of("message", "사용자가 접속했습니다.")
        );

        broadcast(projectId, joinMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long projectId = getProjectId(session);
        Long userId = getUserId(session);

        RealtimeEventMessage receivedMessage =
                objectMapper.readValue(message.getPayload(), RealtimeEventMessage.class);

        RealtimeEventMessage broadcastMessage = RealtimeEventMessage.of(
                receivedMessage.type(),
                projectId,
                userId,
                receivedMessage.payload() == null ? Collections.emptyMap() : receivedMessage.payload()
        );

        broadcast(projectId, broadcastMessage);
    }

    public void publish(
            Long projectId,
            RealtimeEventType type,
            Long userId,
            Map<String, Object> payload
    ) throws IOException {
        RealtimeEventMessage eventMessage = RealtimeEventMessage.of(
                type,
                projectId,
                userId,
                payload == null ? Collections.emptyMap() : payload
        );

        broadcast(projectId, eventMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long projectId = getProjectId(session);
        Long userId = getUserId(session);

        Set<WebSocketSession> sessions = projectSessions.get(projectId);

        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                projectSessions.remove(projectId);
            }
        }

        RealtimeEventMessage leftMessage = RealtimeEventMessage.of(
                RealtimeEventType.USER_LEFT,
                projectId,
                userId,
                Map.of("message", "사용자가 연결을 종료했습니다.")
        );

        broadcast(projectId, leftMessage);
    }

    private void broadcast(Long projectId, RealtimeEventMessage eventMessage) throws IOException {
        Set<WebSocketSession> sessions = projectSessions.get(projectId);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload = objectMapper.writeValueAsString(eventMessage);
        TextMessage textMessage = new TextMessage(payload);

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                } else {
                    sessions.remove(session);
                }
            } catch (IOException e) {
                sessions.remove(session);
            }
        }

        if (sessions.isEmpty()) {
            projectSessions.remove(projectId);
        }
    }

    private Long extractProjectId(WebSocketSession session) {
        URI uri = session.getUri();

        if (uri == null) {
            throw new IllegalArgumentException("WebSocket URI를 확인할 수 없습니다.");
        }

        String path = uri.getPath();
        String[] parts = path.split("/");

        try {
            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("projectId는 숫자여야 합니다.");
        }
    }

    private Long extractUserId(WebSocketSession session) {
        URI uri = session.getUri();

        if (uri == null || uri.getQuery() == null) {
            throw new IllegalArgumentException("WebSocket 연결 시 userId 쿼리 파라미터가 필요합니다.");
        }

        String[] queryParams = uri.getQuery().split("&");

        for (String queryParam : queryParams) {
            String[] keyValue = queryParam.split("=");

            if (keyValue.length == 2 && keyValue[0].equals("userId")) {
                try {
                    return Long.parseLong(keyValue[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("userId는 숫자여야 합니다.");
                }
            }
        }

        throw new IllegalArgumentException("WebSocket 연결 시 userId 쿼리 파라미터가 필요합니다.");
    }

    private Long getProjectId(WebSocketSession session) {
        return (Long) session.getAttributes().get(PROJECT_ID_ATTRIBUTE);
    }

    private Long getUserId(WebSocketSession session) {
        return (Long) session.getAttributes().get(USER_ID_ATTRIBUTE);
    }
}