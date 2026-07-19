package com.team1.__spring_team1.domain.stage.entity;

/**
 * AI 생성 단계 구분용 enum.
 * ProjectStage(MEETING~COMPLETED, 프로젝트 전체 진행 상태)와는 별개로,
 * AI에게 "지금 무슨 단계를 생성하는지"를 알려주는 용도로 사용한다.
 *
 * PLAN / FEATURE_SPEC / SCREEN_SPEC은 STAGE_DOCUMENTS.stage_type 컬럼에 저장되지만,
 * WIREFRAME은 wireframe 테이블의 별도 엔티티로 저장되며 stage_document로 남지 않는다.
 * 따라서 WIREFRAME을 stage_type 컬럼 값으로 사용해서는 안 된다.
 */
public enum StageType {
    PLAN,
    FEATURE_SPEC,
    SCREEN_SPEC,
    WIREFRAME
}