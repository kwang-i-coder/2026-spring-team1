package com.team1.__spring_team1.domain.stage.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.entity.StageDocumentStatus;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

/**
 * 문서 생성(generate) 및 조회(GET) 공통 응답 DTO.
 *
 * content는 DB에 JSON 문자열로 저장돼 있지만,
 * API 응답에서는 Object로 반환해서 프론트가 JSON 객체로 받을 수 있게 한다.
 */
@Getter
@Builder
public class StageDocumentResponse {

    private Long documentId;
    private StageType stageType;
    private StageDocumentStatus status;
    private Object content;  // PlanContent / FeatureSpecContent / ScreenSpecContent

    public static StageDocumentResponse of(StageDocument document, ObjectMapper objectMapper) {
        Object parsedContent;
        try {
            parsedContent = objectMapper.readValue(document.getContent(), Object.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }

        return StageDocumentResponse.builder()
                .documentId(document.getId())
                .stageType(document.getStageType())
                .status(document.getStatus())
                .content(parsedContent)
                .build();
    }
}