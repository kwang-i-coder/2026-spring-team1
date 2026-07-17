package com.team1.__spring_team1.global.security;

import com.team1.__spring_team1.domain.auth.entity.Session;
import com.team1.__spring_team1.domain.auth.repository.SessionRepository;
import com.team1.__spring_team1.domain.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionAuthFilter extends OncePerRequestFilter {

    private static final String SESSION_COOKIE_NAME = "SESSION";

    private final SessionRepository sessionRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        extractSessionToken(request)
                .map(TokenHashUtil::hash)
                .flatMap(sessionRepository::findBySessionTokenHash)
                .filter(Session::isValid)
                .ifPresent(this::setAuthentication);

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractSessionToken(
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie ->
                        SESSION_COOKIE_NAME.equals(cookie.getName())
                )
                .map(Cookie::getValue)
                .findFirst();
    }

    private void setAuthentication(Session session) {
        User user = session.getUser();

        LoginUser loginUser = new LoginUser(
                user.getId(),
                user.getLoginId(),
                user.getName()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        loginUser,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}