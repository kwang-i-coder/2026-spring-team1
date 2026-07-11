package com.team1.__spring_team1.domain.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.domain.infrastructure.ai.GeminiApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 실제 Gemini API를 호출하는 AiDocumentClient 구현체.
 * prod 환경에서만 활성화된다.
 *
 * Gemini API 응답 구조:
 * {
 *   "candidates": [
 *     {
 *       "content": {
 *         "parts": [
 *           { "text": "{ ... AI가 생성한 JSON 문자열 ... }" }
 *         ]
 *       }
 *     }
 *   ]
 * }
 *
 * candidates[0].content.parts[0].text 에서 content JSON 문자열을 추출한다.
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class GeminiAiDocumentClient implements AiDocumentClient {

    @Qualifier("geminiRestClient")
    private final RestClient restClient;
    private final GeminiApiProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public String generate(String prompt, StageType stageType) {
        // Gemini API 요청 body 구성
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "responseMimeType", "application/json"
                )
        );

        String uri = "/v1beta/models/" + properties.getModel()
                + ":generateContent?key=" + properties.getApiKey();

        try {
            // Gemini API 호출
            String rawResponse = restClient.post()
                    .uri(uri)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // candidates[0].content.parts[0].text 추출
            JsonNode root = objectMapper.readTree(rawResponse);
            String contentJson = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();

            log.debug("[GeminiAiDocumentClient] stageType={}, response={}", stageType, contentJson);
            return contentJson;

        } catch (Exception e) {
            log.error("[GeminiAiDocumentClient] AI 생성 실패. stageType={}, error={}", stageType, e.getMessage());
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED);
        }
    }
}