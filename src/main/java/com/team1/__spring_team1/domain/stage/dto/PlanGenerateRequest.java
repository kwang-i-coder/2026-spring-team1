package com.team1.__spring_team1.domain.stage.dto;

import com.team1.__spring_team1.domain.stage.entity.SourceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * POST /projects/{projectId}/stages/plan/generate 요청 DTO.
 * 회의록 또는 STT 변환 파일 중 어느 것을 source로 사용할지 지정한다.
 */
@Getter
@NoArgsConstructor
public class PlanGenerateRequest {

    @NotNull
    private SourceType sourceType;  // MEETING_NOTE or MEETING_FILE

    @NotNull
    private Long sourceId;
}