package com.team1.__spring_team1.domain.stage.entity;

/**
 * STAGE_DOCUMENTS.stage_type 컬럼 매핑용 enum.
 * ProjectStage(MEETING~COMPLETED, 프로젝트 전체 진행 상태)와는 별개로,
 * AI가 생성하는 "문서의 종류"만 구분한다.
 */
public enum StageType {
    PLAN,
    FEATURE_SPEC,
    SCREEN_SPEC
}