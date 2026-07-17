package com.team1.__spring_team1.domain.stage.entity;

/**
 * STAGE_DOCUMENTS.source_type 컬럼 매핑용 enum.
 * source_id가 어느 테이블의 PK를 가리키는지 구분한다.
 *
 * MEETING_NOTE   - meeting_notes 테이블 참조 (1단계 PLAN 생성 시)
 * MEETING_FILE   - meeting_files 테이블 참조 (1단계 PLAN 생성 시, STT 원본 파일)
 * STAGE_DOCUMENT - stage_documents 테이블 참조 (2단계 FEATURE_SPEC, 3단계 SCREEN_SPEC 생성 시,
 *                  이전 단계 확정 문서를 source로 사용)
 */
public enum SourceType {
    MEETING_NOTE,
    MEETING_FILE,
    STAGE_DOCUMENT
}