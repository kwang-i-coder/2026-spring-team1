package com.team1.__spring_team1.global.security;

import com.team1.__spring_team1.domain.auth.entity.Session;
import com.team1.__spring_team1.domain.auth.repository.SessionRepository;
import com.team1.__spring_team1.domain.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriUtils;
import org.springframework.stereotype.Component;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class SessionHandshakeInterceptor implements HandshakeInterceptor {

    private static final String SESSION_COOKIE_NAME = "SESSION";
    private static final String PROJECT_ID_ATTRIBUTE = "projectId";
    private static final String USER_ID_ATTRIBUTE = "userId";

    private final SessionRepository sessionRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Long projectId = extractProjectId(request.getURI());
        String sessionToken = extractSessionTokenFromCookie(request)
                .orElseGet(() -> extractSessionTokenFromQuery(request.getURI()));

        if (projectId == null || sessionToken == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Session session = sessionRepository
                .findBySessionTokenHash(TokenHashUtil.hash(sessionToken))
                .filter(Session::isValid)
                .orElse(null);

        if (session == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = session.getUser().getId();
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        attributes.put(PROJECT_ID_ATTRIBUTE, projectId);
        attributes.put(USER_ID_ATTRIBUTE, userId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // No cleanup is required because the handler owns active session tracking.
    }

    private java.util.Optional<String> extractSessionTokenFromCookie(ServerHttpRequest request) {
        return request.getHeaders()
                .getOrDefault(HttpHeaders.COOKIE, List.of())
                .stream()
                .map(cookie -> parseCookie(cookie, SESSION_COOKIE_NAME))
                .filter(cookie -> cookie != null)
                .findFirst()
                .map(HttpCookie::getValue);
    }
    private String extractSessionTokenFromQuery(URI uri) {
        if (uri == null) {
            return null;
        }
        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("token");
    }

    private HttpCookie parseCookie(String cookieHeader, String cookieName) {
        for (String cookiePart : cookieHeader.split(";")) {
            String[] nameAndValue = cookiePart.trim().split("=", 2);
            if (nameAndValue.length == 2 && cookieName.equals(nameAndValue[0])) {
                return new HttpCookie(
                        cookieName,
                        UriUtils.decode(nameAndValue[1], StandardCharsets.UTF_8)
                );
            }
        }
        return null;
    }

    private Long extractProjectId(URI uri) {
        if (uri == null || uri.getPath() == null) {
            return null;
        }

        String[] pathParts = uri.getPath().split("/");
        if (pathParts.length == 0) {
            return null;
        }

        try {
            return Long.parseLong(pathParts[pathParts.length - 1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
