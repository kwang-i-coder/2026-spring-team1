
package com.team1.__spring_team1.domain.stage.entity;

/**
 * STAGE_DOCUMENTS.status 컬럼 매핑용 enum.
 *
 * DRAFT     - AI 생성 직후, 공동편집 진행 중인 상태
 * CONFIRMED - leader가 확정한 상태 (confirmed_by/confirmed_at 기록됨)
 */
public enum StageDocumentStatus {
    DRAFT,
    CONFIRMED
}