package com.team1.__spring_team1.global.config;

import com.team1.__spring_team1.domain.realtime.handler.ProjectWebSocketHandler;
import com.team1.__spring_team1.global.security.SessionHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ProjectWebSocketHandler projectWebSocketHandler;
    private final SessionHandshakeInterceptor sessionHandshakeInterceptor;
    private final FrontendOriginProvider frontendOriginProvider;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(projectWebSocketHandler, "/ws/projects/*")
                .addInterceptors(sessionHandshakeInterceptor)
                .setAllowedOrigins(frontendOriginProvider.getOrigin());
    }
}
