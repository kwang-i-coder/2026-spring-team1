package com.team1.__spring_team1.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.ai.client.AiDocumentClient;
import com.team1.__spring_team1.domain.ai.dto.FeatureSpecContent;
import com.team1.__spring_team1.domain.ai.dto.PlanContent;
import com.team1.__spring_team1.domain.ai.dto.ScreenSpecContent;
import com.team1.__spring_team1.domain.ai.prompt.FeatureSpecPromptBuilder;
import com.team1.__spring_team1.domain.ai.prompt.PlanPromptBuilder;
import com.team1.__spring_team1.domain.ai.prompt.ScreenSpecPromptBuilder;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 문서 생성 오케스트레이션 서비스.
 * StageService에서 호출하며, 내부적으로:
 * 1. stageType에 맞는 PromptBuilder로 prompt 조립
 * 2. AiDocumentClient로 AI 호출 (Mock or Gemini)
 * 3. 응답 JSON 파싱
 * 4. 파싱 실패 시 AI_RESPONSE_INVALID 에러
 *
 * StageService는 이 서비스만 의존하고,
 * 내부적으로 Mock인지 Gemini인지, 어떤 prompt를 쓰는지 알 필요 없다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDocumentService {

    private final AiDocumentClient aiDocumentClient;
    private final PlanPromptBuilder planPromptBuilder;
    private final FeatureSpecPromptBuilder featureSpecPromptBuilder;
    private final ScreenSpecPromptBuilder screenSpecPromptBuilder;
    private final ObjectMapper objectMapper;

    /**
     * 회의자료 텍스트로 PLAN(기획서) 생성.
     *
     * @param sourceContent 회의록 또는 STT 변환 텍스트
     * @return 파싱된 PlanContent 객체
     */
    public PlanContent generatePlan(String sourceContent) {
        String prompt = planPromptBuilder.build(sourceContent);
        String json = aiDocumentClient.generate(prompt, StageType.PLAN);
        return parse(json, PlanContent.class);
    }

    /**
     * 확정된 기획서로 FEATURE_SPEC(기능명세서) 생성.
     *
     * @param planContent 확정된 PLAN 문서의 content JSON 문자열
     * @return 파싱된 FeatureSpecContent 객체
     */
    public FeatureSpecContent generateFeatureSpec(String planContent) {
        String prompt = featureSpecPromptBuilder.build(planContent);
        String json = aiDocumentClient.generate(prompt, StageType.FEATURE_SPEC);
        return parse(json, FeatureSpecContent.class);
    }

    /**
     * 확정된 기능명세서로 SCREEN_SPEC(화면별 기획서) 생성.
     *
     * @param featureSpecContent 확정된 FEATURE_SPEC 문서의 content JSON 문자열
     * @return 파싱된 ScreenSpecContent 객체
     */
    public ScreenSpecContent generateScreenSpec(String featureSpecContent) {
        String prompt = screenSpecPromptBuilder.build(featureSpecContent);
        String json = aiDocumentClient.generate(prompt, StageType.SCREEN_SPEC);
        return parse(json, ScreenSpecContent.class);
    }

    /**
     * AI 응답 JSON 문자열을 지정된 타입으로 역직렬화한다.
     * 파싱 실패 시 AI_RESPONSE_INVALID 에러를 던진다.
     */
    private <T> T parse(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("[AiDocumentService] AI 응답 파싱 실패. type={}, json={}, error={}",
                    type.getSimpleName(), json, e.getMessage());
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}