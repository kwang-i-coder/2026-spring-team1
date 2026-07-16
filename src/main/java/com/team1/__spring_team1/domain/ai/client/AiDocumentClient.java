package com.team1.__spring_team1.domain.ai.client;

import com.team1.__spring_team1.domain.stage.entity.StageType;

/**
 * AI 문서 생성 클라이언트 인터페이스.
 * Mock과 Gemini 두 가지 구현체를 가진다.
 *
 * StageService -> AiDocumentService -> AiDocumentClient 순으로 호출되며,
 * StageService는 내부적으로 Mock인지 Gemini인지 알 필요가 없다.
 */
public interface AiDocumentClient {

    /**
     * 주어진 prompt를 AI에 전달하고 JSON 문자열 응답을 반환한다.
     * 응답은 stageType에 맞는 content JSON 구조여야 한다.
     *
     * @param prompt    단계별 PromptBuilder가 조립한 최종 prompt 문자열
     * @param stageType 생성할 문서 단계 (PLAN / FEATURE_SPEC / SCREEN_SPEC)
     * @return AI가 반환한 content JSON 문자열
     */
    String generate(String prompt, StageType stageType);
}
