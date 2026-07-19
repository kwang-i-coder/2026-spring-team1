package com.team1.__spring_team1.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class FrontendOriginProvider {

    private final String origin;

    public FrontendOriginProvider(@Value("${app.frontend.base-url}") String frontendBaseUrl) {
        this.origin = frontendBaseUrl.strip().replaceAll("/+$", "");
    }
}
