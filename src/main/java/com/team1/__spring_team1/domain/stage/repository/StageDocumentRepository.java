package com.team1.__spring_team1.domain.stage.repository;

import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.entity.StageDocumentStatus;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StageDocumentRepository extends JpaRepository<StageDocument, Long> {

    // 특정 프로젝트의 특정 단계 문서 조회
    // GET /projects/{projectId}/satge-documents/{stageType}
    Optional<StageDocument> findTopByProjectIdAndStageTypeOrderByCreatedAtDesc(Long projectId, StageType stageType);

    //이전 단계 확정 여부 검증용
    // generate 호출 전, 이전 stageType의 CONFIRMED 문서가 존재하는지 체크
    boolean existsByProjectIdAndStageTypeAndStatus(Long projectId, StageType stageType, StageDocumentStatus status);
}