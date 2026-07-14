package com.team1.__spring_team1.domain.stage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.ai.dto.FeatureSpecContent;
import com.team1.__spring_team1.domain.ai.dto.PlanContent;
import com.team1.__spring_team1.domain.ai.dto.ScreenSpecContent;
import com.team1.__spring_team1.domain.ai.service.AiDocumentService;
import com.team1.__spring_team1.domain.meeting.entity.MeetingFile;
import com.team1.__spring_team1.domain.meeting.entity.MeetingNote;
import com.team1.__spring_team1.domain.meeting.repository.MeetingFileRepository;
import com.team1.__spring_team1.domain.meeting.repository.MeetingNoteRepository;
import com.team1.__spring_team1.domain.stage.dto.FeatureGenerateRequest;
import com.team1.__spring_team1.domain.stage.dto.PlanGenerateRequest;
import com.team1.__spring_team1.domain.stage.dto.ScreenGenerateRequest;
import com.team1.__spring_team1.domain.stage.dto.SnapshotUpdateRequest;
import com.team1.__spring_team1.domain.stage.dto.StageDocumentResponse;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.entity.SourceType;
import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.entity.StageDocumentStatus;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.stage.repository.StageDocumentRepository;
import com.team1.__spring_team1.domain.project.entity.ProjectMember;
import com.team1.__spring_team1.domain.project.repository.ProjectMemberRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StageService {

    private final StageDocumentRepository stageDocumentRepository;
    private final ScreenRepository screenRepository;
    private final AiDocumentService aiDocumentService;
    private final MeetingNoteRepository meetingNoteRepository;
    private final MeetingFileRepository meetingFileRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ObjectMapper objectMapper;

    // ─────────────────────────────────────────
    // 1단계: PLAN 생성
    // ─────────────────────────────────────────

    @Transactional
    public StageDocumentResponse generatePlan(Long projectId, PlanGenerateRequest request, LoginUser loginUser) {
        // 프로젝트 멤버 권한 검증
        ProjectMember member = getProjectMember(projectId, loginUser.userId());
        if (!member.isMember()) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        if (stageDocumentRepository.existsByProjectIdAndStageTypeAndStatus(
                projectId, StageType.PLAN, StageDocumentStatus.CONFIRMED)) {
            throw new BusinessException(ErrorCode.STAGE_COMPLETED);
        }

        // source 텍스트 조회
        String sourceContent = getSourceContent(request.getSourceType(), request.getSourceId());

        // AI로 PLAN 생성
        PlanContent planContent = aiDocumentService.generatePlan(sourceContent);

        // content 직렬화 후 StageDocument 저장
        String contentJson = serialize(planContent);
        StageDocument document = StageDocument.createDraft(
                projectId,
                StageType.PLAN,
                contentJson,
                request.getSourceType(),
                request.getSourceId(),
                loginUser.userId()
        );
        stageDocumentRepository.save(document);

        return StageDocumentResponse.of(document, objectMapper);
    }

    // ─────────────────────────────────────────
    // 2단계: FEATURE_SPEC 생성
    // ─────────────────────────────────────────

    @Transactional
    public StageDocumentResponse generateFeatureSpec(Long projectId, FeatureGenerateRequest request, LoginUser loginUser) {
        // 프로젝트 멤버 권한 검증
        ProjectMember member = getProjectMember(projectId, loginUser.userId());
        if (!member.isMember()) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        if (stageDocumentRepository.existsByProjectIdAndStageTypeAndStatus(
                projectId, StageType.FEATURE_SPEC, StageDocumentStatus.CONFIRMED)) {
            throw new BusinessException(ErrorCode.STAGE_COMPLETED);
        }

        // 이전 단계 문서 조회
        StageDocument previousDocument = stageDocumentRepository
                .findByIdAndProjectIdAndStageTypeAndStatus(
                        request.getPreviousDocumentId(),
                        projectId,
                        StageType.PLAN,
                        StageDocumentStatus.CONFIRMED)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_STEP_ORDER_INVALID));

        // AI로 FEATURE_SPEC 생성
        FeatureSpecContent featureSpecContent = aiDocumentService.generateFeatureSpec(previousDocument.getContent());

        // content 직렬화 후 StageDocument 저장
        String contentJson = serialize(featureSpecContent);
        StageDocument document = StageDocument.createDraft(
                projectId,
                StageType.FEATURE_SPEC,
                contentJson,
                SourceType.STAGE_DOCUMENT,
                request.getPreviousDocumentId(),
                loginUser.userId()
        );
        stageDocumentRepository.save(document);

        return StageDocumentResponse.of(document, objectMapper);
    }

    // ─────────────────────────────────────────
    // 3단계: SCREEN_SPEC 생성
    // ─────────────────────────────────────────

    @Transactional
    public StageDocumentResponse generateScreenSpec(Long projectId, ScreenGenerateRequest request, LoginUser loginUser) {
        // 프로젝트 멤버 권한 검증
        ProjectMember member = getProjectMember(projectId, loginUser.userId());
        if (!member.isMember()) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        if (stageDocumentRepository.existsByProjectIdAndStageTypeAndStatus(
                projectId, StageType.SCREEN_SPEC, StageDocumentStatus.CONFIRMED)) {
            throw new BusinessException(ErrorCode.STAGE_COMPLETED);
        }

        // 이전 단계 문서 조회
        StageDocument previousDocument = stageDocumentRepository
                .findByIdAndProjectIdAndStageTypeAndStatus(request.getPreviousDocumentId(), projectId, StageType.FEATURE_SPEC, StageDocumentStatus.CONFIRMED)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_STEP_ORDER_INVALID));

        // AI로 SCREEN_SPEC 생성
        ScreenSpecContent screenSpecContent = aiDocumentService.generateScreenSpec(previousDocument.getContent());

        // content 직렬화 후 StageDocument 저장
        String contentJson = serialize(screenSpecContent);
        StageDocument document = StageDocument.createDraft(
                projectId,
                StageType.SCREEN_SPEC,
                contentJson,
                SourceType.STAGE_DOCUMENT,
                request.getPreviousDocumentId(),
                loginUser.userId()
        );
        stageDocumentRepository.save(document);

        return StageDocumentResponse.of(document, objectMapper);
    }

    // ─────────────────────────────────────────
    // 단계 문서 조회
    // ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public StageDocumentResponse getStageDocument(Long projectId, StageType stageType, LoginUser loginUser) {
        // 프로젝트 멤버 권한 검증
        ProjectMember member = getProjectMember(projectId, loginUser.userId());
        if (!member.isMember()) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        StageDocument document = stageDocumentRepository
                .findTopByProjectIdAndStageTypeOrderByCreatedAtDesc(projectId, stageType)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_DOCUMENT_NOT_FOUND));

        return StageDocumentResponse.of(document, objectMapper);
    }

    // ─────────────────────────────────────────
    // snapshot 저장
    // ─────────────────────────────────────────

    @Transactional
    public void updateSnapshot(Long documentId, SnapshotUpdateRequest request, LoginUser loginUser) {
        StageDocument document = stageDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_DOCUMENT_NOT_FOUND));

        // 프로젝트 멤버 권한 검증
        ProjectMember member = getProjectMember(document.getProjectId(), loginUser.userId());
        if (!member.isMember()) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        // 이미 확정된 문서는 수정 불가
        if (document.isConfirmed()) {
            throw new BusinessException(ErrorCode.STAGE_COMPLETED);
        }

        document.updateSnapshot(request.getContent());
    }

    // ─────────────────────────────────────────
    // 단계 확정
    // ─────────────────────────────────────────

    @Transactional
    public void confirmDocument(Long documentId, LoginUser loginUser) {
        StageDocument document = stageDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_DOCUMENT_NOT_FOUND));

        // 리더 권한 검증
        ProjectMember member = getProjectMember(document.getProjectId(), loginUser.userId());
        if (!member.isLeader()) {
            throw new BusinessException(ErrorCode.PROJECT_LEADER_ONLY);
        }

        // 이미 확정된 문서는 재확정 불가
        if (document.isConfirmed()) {
            throw new BusinessException(ErrorCode.STAGE_COMPLETED);
        }

        document.confirm(loginUser.userId());

        // SCREEN_SPEC 확정 시 Screen 엔티티 추출 및 저장
        if (document.getStageType() == StageType.SCREEN_SPEC) {
            extractAndSaveScreens(document);
        }
    }

    // ─────────────────────────────────────────
    // 회광님(F파트)을 위한 메서드
    // ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public StageDocument getConfirmedScreenSpec(Long projectId) {
        return stageDocumentRepository
                .findTopByProjectIdAndStageTypeAndStatusOrderByCreatedAtDesc(
                        projectId,
                        StageType.SCREEN_SPEC,
                        StageDocumentStatus.CONFIRMED)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_DOCUMENT_NOT_FOUND));
    }

    // ─────────────────────────────────────────
    // private 헬퍼 메서드
    // ─────────────────────────────────────────

    private void extractAndSaveScreens(StageDocument document) {
        try {
            ScreenSpecContent screenSpecContent = objectMapper.readValue(
                    document.getContent(), ScreenSpecContent.class);

            List<Screen> screens = IntStream.range(0, screenSpecContent.getScreens().size())
                    .mapToObj(i -> {
                        ScreenSpecContent.ScreenItem item = screenSpecContent.getScreens().get(i);
                        String specJson = serialize(item);
                        return Screen.create(
                                document.getProjectId(),
                                document.getId(),
                                item.getName(),
                                item.getPurpose(),
                                i + 1,
                                specJson
                        );
                    })
                    .toList();

            screenRepository.saveAll(screens);
            log.info("[StageService] SCREEN_SPEC 확정 완료. documentId={}, 추출된 화면 수={}",
                    document.getId(), screens.size());

        } catch (JsonProcessingException e) {
            log.error("[StageService] Screen 추출 실패. documentId={}, error={}", document.getId(), e.getMessage());
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }

    private String getSourceContent(SourceType sourceType, Long sourceId) {
        return switch (sourceType) {
            case MEETING_NOTE -> {
                MeetingNote note = meetingNoteRepository.findById(sourceId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOTE_NOT_FOUND));
                yield note.getContent();
            }
            case MEETING_FILE -> {
                MeetingFile file = meetingFileRepository.findById(sourceId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_FILE_NOT_FOUND));
                yield file.getTranscript();
            }
            case STAGE_DOCUMENT -> throw new BusinessException(ErrorCode.STAGE_TYPE_INVALID);
        };
    }

    private ProjectMember getProjectMember(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PROJECT_MEMBER));
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[StageService] 직렬화 실패. obj={}", obj.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}