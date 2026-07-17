package com.team1.__spring_team1.domain.stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * POST /projects/{projectId}/stages/screens/generate 요청 DTO.
 * 확정된 FEATURE_SPEC 문서 ID를 받아서 화면별 기획서 생성의 source로 사용한다.
 */
@Getter
@NoArgsConstructor
public class ScreenGenerateRequest {

    @NotNull
    private Long previousDocumentId;  // 확정된 FEATURE_SPEC 문서 ID
}