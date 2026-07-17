package com.team1.__spring_team1.domain.export.service;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.entity.StageDocumentStatus;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.stage.repository.StageDocumentRepository;
import com.team1.__spring_team1.domain.wireframe.entity.Wireframe;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeStatus;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarkdownExportService {

    private final ProjectPermissionService projectPermissionService;
    private final StageDocumentRepository stageDocumentRepository;
    private final ScreenRepository screenRepository;
    private final WireframeRepository wireframeRepository;

    public String exportMarkdown(Long projectId, LoginUser loginUser) {
        projectPermissionService.validateProjectMember(projectId, loginUser.userId());
        Project project = projectPermissionService.validateProjectExists(projectId);

        StageDocument plan = getConfirmedDocument(projectId, StageType.PLAN);
        StageDocument featureSpec = getConfirmedDocument(projectId, StageType.FEATURE_SPEC);
        StageDocument screenSpec = getConfirmedDocument(projectId, StageType.SCREEN_SPEC);

        List<Screen> screens = screenRepository.findByStageDocumentIdOrderByScreenOrder(screenSpec.getId());

        if (screens.isEmpty()) {
            throw new BusinessException(ErrorCode.EXPORT_NOT_READY);
        }

        Map<Long, Wireframe> wireframeByScreenId = wireframeRepository
                .findAllByProjectIdAndStatus(projectId, WireframeStatus.COMPLETED)
                .stream()
                .collect(Collectors.toMap(
                        Wireframe::getScreenId,
                        Function.identity(),
                        (first, second) -> first.getVersion() >= second.getVersion() ? first : second
                ));

        StringBuilder markdown = new StringBuilder();

        markdown.append("# ").append(project.getTitle()).append(" 최종 리포트\n\n");
        appendProjectInfo(markdown, project);

        appendJsonSection(markdown, "1. 기획서", plan.getContent());
        appendJsonSection(markdown, "2. 기능 명세서", featureSpec.getContent());
        appendJsonSection(markdown, "3. 화면별 기획서", screenSpec.getContent());

        markdown.append("## 4. 화면별 Spec\n\n");
        for (Screen screen : screens) {
            markdown.append("### ")
                    .append(screen.getScreenOrder())
                    .append(". ")
                    .append(screen.getName())
                    .append("\n\n");

            if (screen.getPurpose() != null && !screen.getPurpose().isBlank()) {
                markdown.append("- 목적: ").append(screen.getPurpose()).append("\n\n");
            }

            appendCodeBlock(markdown, screen.getSpecJson());
        }

        markdown.append("## 5. 와이어프레임 JSON DSL\n\n");
        for (Screen screen : screens) {
            Wireframe wireframe = wireframeByScreenId.get(screen.getId());

            if (wireframe == null) {
                throw new BusinessException(ErrorCode.EXPORT_NOT_READY);
            }

            markdown.append("### ")
                    .append(screen.getScreenOrder())
                    .append(". ")
                    .append(screen.getName())
                    .append("\n\n");

            appendCodeBlock(markdown, wireframe.getJsonDsl());
        }

        return markdown.toString();
    }

    private StageDocument getConfirmedDocument(Long projectId, StageType stageType) {
        return stageDocumentRepository
                .findTopByProjectIdAndStageTypeAndStatusOrderByCreatedAtDesc(
                        projectId,
                        stageType,
                        StageDocumentStatus.CONFIRMED
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPORT_NOT_READY));
    }

    private void appendProjectInfo(StringBuilder markdown, Project project) {
        markdown.append("## 프로젝트 정보\n\n");
        markdown.append("- 설명: ").append(nullToBlank(project.getDescription())).append("\n");
        markdown.append("- 목표: ").append(nullToBlank(project.getGoal())).append("\n");
        markdown.append("- 시작일: ").append(project.getStartDate()).append("\n");
        markdown.append("- 종료일: ").append(project.getEndDate()).append("\n\n");
    }

    private void appendJsonSection(StringBuilder markdown, String title, String content) {
        markdown.append("## ").append(title).append("\n\n");
        appendCodeBlock(markdown, content);
    }

    private void appendCodeBlock(StringBuilder markdown, String content) {
        markdown.append("```json\n")
                .append(content == null ? "" : content)
                .append("\n```\n\n");
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}