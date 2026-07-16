package com.team1.__spring_team1.domain.infrastructure.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiRestClientConfig {

    @Bean(name = "geminiRestClient")
    public RestClient geminiRestClient(GeminiApiProperties properties){
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Content-Type", "applicaiton/json")
                .build();
    }
}
