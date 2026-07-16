package com.team1.__spring_team1.domain.stage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PATCH /stage-documents/{documentId}/snapshot 요청 DTO.
 * 공동편집으로 수정된 content를 저장한다.
 * content는 JSON 객체를 문자열로 직렬화한 값이다.
 */
@Getter
@NoArgsConstructor
public class SnapshotUpdateRequest {

    @NotBlank
    private String content;
}