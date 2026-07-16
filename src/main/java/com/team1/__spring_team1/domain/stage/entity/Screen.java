package com.team1.__spring_team1.domain.stage.entity;

import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * screens 테이블 매핑 엔티티.
 * SCREEN_SPEC(3단계) 문서가 확정되는 시점에 StageDocument.content를 파싱하여
 * 화면 단위로 추출/저장된다. wireframe(4단계)이 screen과 1:1로 대응한다.
 *
 * 연관관계는 @ManyToOne 없이 Long FK로 직접 저장한다.
 * (projectId, stageDocumentId)
 */
@Entity
@Table(name = "screens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Screen extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "stage_document_id", nullable = false)
    private Long stageDocumentId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "screen_order", nullable = false)
    private Integer screenOrder;

    /**
     * 화면별 상세 spec. ER 스키마상 LONGTEXT이지만 PostgreSQL은 TEXT로 매핑한다.
     */
    @Column(name = "spec_json", columnDefinition = "TEXT", nullable = false)
    private String specJson;

    @Builder
    private Screen(Long projectId, Long stageDocumentId, String name, String purpose,
                   Integer screenOrder, String specJson) {
        this.projectId = projectId;
        this.stageDocumentId = stageDocumentId;
        this.name = name;
        this.purpose = purpose;
        this.screenOrder = screenOrder;
        this.specJson = specJson;
    }

    public static Screen create(Long projectId, Long stageDocumentId, String name, String purpose,
                                Integer screenOrder, String specJson) {
        return Screen.builder()
                .projectId(projectId)
                .stageDocumentId(stageDocumentId)
                .name(name)
                .purpose(purpose)
                .screenOrder(screenOrder)
                .specJson(specJson)
                .build();
    }
}