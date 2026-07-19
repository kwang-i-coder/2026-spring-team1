package com.team1.__spring_team1.domain.stage.entity;

import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * stage_documents 테이블 매핑 엔티티.
 * PLAN / FEATURE_SPEC / SCREEN_SPEC 세 단계 문서를 stageType으로 구분하여 통합 관리한다.
 *
 * 연관관계는 @ManyToOne 없이 Long FK로 직접 저장한다.
 * (projectId, sourceId, createdBy, confirmedBy)
 *
 * 상태 전이/단계 순서 검증 등 비즈니스 규칙은 StageService에서 처리하고,
 * 엔티티는 상태 변경 동작(updateSnapshot, confirm)만 제공한다.
 */

@Entity
@Table(name="stage_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", nullable = false, length = 20)
    private StageType stageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StageDocumentStatus status;

    /**
     * AI가 생성한(또는 공동편집으로 수정된) 문서 본문.
     * ER 스키마상 LONGTEXT이지만 PostgreSQL은 LONGTEXT 타입이 없으므로 TEXT로 매핑한다.
     */

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Builder
    private StageDocument(Long projectId, StageType stageType, String content, SourceType sourceType, Long sourceId, Long createdBy) {
        this.projectId = projectId;
        this.stageType = stageType;
        this.status = StageDocumentStatus.DRAFT;
        this.content = content;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.createdBy = createdBy;
    }

    /**
     * AI 생성 직후 DRAFT 상태로 문서를 새로 만든다.
     */
    public static StageDocument createDraft(Long projectId, StageType stageType, String content, SourceType sourceType, Long sourceId, Long createdBy) {
        return StageDocument.builder()
                .projectId(projectId)
                .stageType(stageType)
                .content(content)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .createdBy(createdBy)
                .build();
    }

    /**
     * 공동편집 결과를 반영한다. (DRAFT 상태에서만 호출되어야 하며, 가능 여부 검증은 Service에서 처리)
     */
    public void updateSnapshot(String content){
        this.content = content;
    }

    /**
     * leader 확정 처리. 확정 가능 여부(권한, 상태) 검증은 Service에서 처리한다.
     */
    public void confirm(Long confirmedId){
        this.status = StageDocumentStatus.CONFIRMED;
        this.confirmedBy = confirmedId;
        this.confirmedAt = LocalDateTime.now();
    }

    public boolean isConfirmed(){
        return this.status == StageDocumentStatus.CONFIRMED;
    }
}
