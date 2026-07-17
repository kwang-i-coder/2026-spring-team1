package com.team1.__spring_team1.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * FEATURE_SPEC(2단계) AI 응답 content 구조.
 * Gemini가 반환하는 JSON을 역직렬화하는 DTO.
 */

@Getter
@NoArgsConstructor
public class FeatureSpecContent {

    private List<Feature> features;

    @Getter
    @NoArgsConstructor
    public static class Feature {
        private String name;
        private String description;
        private String priority; // HIGH, MEDIUM, LOW
        private boolean includedInMvp;
    }
}
