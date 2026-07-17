package com.team1.__spring_team1.domain.infrastructure.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yml의 gemini 설정을 바인딩하는 Properties 클래스.
 */

@Getter
@Setter
@Component
@ConfigurationProperties(prefix="gemini")
public class GeminiApiProperties {

    private String apiKey;
    private String baseUrl;
    private String model;
}
