package com.team1.__spring_team1.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.ai.client.AiDocumentClient;
import com.team1.__spring_team1.domain.ai.dto.FeatureSpecContent;
import com.team1.__spring_team1.domain.ai.dto.PlanContent;
import com.team1.__spring_team1.domain.ai.dto.ScreenSpecContent;
import com.team1.__spring_team1.domain.ai.dto.WireframeContent;
import com.team1.__spring_team1.domain.ai.prompt.FeatureSpecPromptBuilder;
import com.team1.__spring_team1.domain.ai.prompt.PlanPromptBuilder;
import com.team1.__spring_team1.domain.ai.prompt.ScreenSpecPromptBuilder;
import com.team1.__spring_team1.domain.ai.prompt.WireframePromptBuilder;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
    private final WireframePromptBuilder wireframePromptBuilder;
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
     * 확정된 화면별 기획서의 화면 하나로 WIREFRAME(와이어프레임) 생성.
     *
     * 앞 단계들과 달리 화면 단위로 호출되며, 화면끼리 서로 의존하지 않는다.
     * 결과는 stage_documents가 아니라 wireframe 테이블에 저장된다.
     *
     * @param screenSpecJson 확정된 Screen 엔티티의 spec_json
     * @return 파싱 및 검증을 통과한 WireframeContent 객체
     */
    public WireframeContent generateWireframe(String screenSpecJson) {
        String prompt = wireframePromptBuilder.build(screenSpecJson);
        String json = aiDocumentClient.generate(prompt, StageType.WIREFRAME);
        WireframeContent content = parse(json, WireframeContent.class);
        validateWireframe(content, json);
        return content;
    }

    /**
     * 와이어프레임 DSL의 유효성을 검증한다.
     *
     * Gemini 요청에 JSON Schema를 넘기지 않기 때문에 파싱에 성공해도
     * 값 자체가 렌더링 불가능한 경우가 있다. 잘못된 DSL이 DB에 저장되면
     * 조회 시점에야 문제가 드러나므로 저장 전에 걸러낸다.
     */
    private void validateWireframe(WireframeContent content, String json) {
        if (isBlank(content.getType())
                || isNotPositive(content.getWidth())
                || isNotPositive(content.getHeight())
                || content.getElements() == null
                || content.getElements().isEmpty()) {
            throw invalidWireframe("canvas 정보가 올바르지 않습니다", json);
        }

        Set<String> ids = new HashSet<>();
        for (WireframeContent.Element element : content.getElements()) {
            if (isInvalidElement(element, content) || !ids.add(element.getId())) {
                throw invalidWireframe("element가 올바르지 않습니다: " + element.getId(), json);
            }
        }
    }

    /**
     * 요소의 필수 필드와 캔버스 경계를 검사한다.
     * 좌표/크기가 null이면 AI가 필드를 누락한 것이므로 함께 걸러낸다.
     */
    private boolean isInvalidElement(WireframeContent.Element element, WireframeContent content) {
        if (isBlank(element.getId())
                || isBlank(element.getType())
                || element.getX() == null
                || element.getY() == null
                || isNotPositive(element.getW())
                || isNotPositive(element.getH())) {
            return true;
        }
        return element.getX() < 0
                || element.getY() < 0
                || element.getX() + element.getW() > content.getWidth()
                || element.getY() + element.getH() > content.getHeight();
    }

    private boolean isNotPositive(Integer value) {
        return value == null || value <= 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BusinessException invalidWireframe(String reason, String json) {
        log.error("[AiDocumentService] 와이어프레임 응답 검증 실패. reason={}, json={}", reason, json);
        return new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
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