package com.team1.__spring_team1.global.config;

import com.team1.__spring_team1.global.security.SessionAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void credentialedCorsAllowsOnlyConfiguredFrontendOrigin() {
        FrontendOriginProvider originProvider =
                new FrontendOriginProvider("https://app.example.com/");
        SecurityConfig securityConfig =
                new SecurityConfig(mock(SessionAuthFilter.class), originProvider);

        CorsConfiguration corsConfiguration = securityConfig
                .corsConfigurationSource()
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/projects"));

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.getAllowedOrigins())
                .containsExactly("https://app.example.com");
        assertThat(corsConfiguration.getAllowCredentials()).isTrue();
        assertThat(corsConfiguration.getAllowedMethods())
                .contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}