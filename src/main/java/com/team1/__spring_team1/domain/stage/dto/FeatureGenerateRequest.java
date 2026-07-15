package com.team1.__spring_team1.domain.stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * POST /projects/{projectId}/stages/features/generate 요청 DTO.
 * 확정된 PLAN 문서 ID를 받아서 기능명세서 생성의 source로 사용한다.
 */
@Getter
@NoArgsConstructor
public class FeatureGenerateRequest {

    @NotNull
    private Long previousDocumentId;  // 확정된 PLAN 문서 ID
}