package com.team1.__spring_team1.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PLAN(1단계) AI 응답 content 구조.
 * Gemini가 반환하는 JSON을 역직렬화하는 DTO.
 *
 * DB에는 ObjectMapper로 직렬화한 JSON 문자열로 저장되고,
 * 조회 시 다시 역직렬화해서 API 응답에 포함된다.
 */

@Getter
@NoArgsConstructor
public class PlanContent {

    private String problemDefinition;
    private String targetUser;
    private String servicePurpose;
    private String coreValue;
}
