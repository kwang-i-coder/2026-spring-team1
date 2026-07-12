package com.team1.__spring_team1.domain.stage.controller;

import com.team1.__spring_team1.domain.stage.dto.FeatureGenerateRequest;
import com.team1.__spring_team1.domain.stage.dto.PlanGenerateRequest;
import com.team1.__spring_team1.domain.stage.dto.ScreenGenerateRequest;
import com.team1.__spring_team1.domain.stage.dto.SnapshotUpdateRequest;
import com.team1.__spring_team1.domain.stage.dto.StageDocumentResponse;
import com.team1.__spring_team1.domain.stage.entity.StageType;
import com.team1.__spring_team1.domain.stage.service.StageService;
import com.team1.__spring_team1.global.response.ApiResponse;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Stage", description = "AI 문서 생성 및 단계 관리 API")
@RestController
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    @Operation(summary = "기획서 생성", description = "회의자료를 기반으로 AI가 기획서 초안을 생성한다.")
    @PostMapping("/projects/{projectId}/stages/plan/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StageDocumentResponse> generatePlan(
            @PathVariable Long projectId,
            @Valid @RequestBody PlanGenerateRequest request,
            @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(stageService.generatePlan(projectId, request, loginUser));
    }

    @Operation(summary = "기능명세서 생성", description = "확정된 기획서를 기반으로 AI가 기능명세서 초안을 생성한다.")
    @PostMapping("/projects/{projectId}/stages/features/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StageDocumentResponse> generateFeatureSpec(
            @PathVariable Long projectId,
            @Valid @RequestBody FeatureGenerateRequest request,
            @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(stageService.generateFeatureSpec(projectId, request, loginUser));
    }

    @Operation(summary = "화면별 기획서 생성", description = "확정된 기능명세서를 기반으로 AI가 화면별 기획서 초안을 생성한다.")
    @PostMapping("/projects/{projectId}/stages/screens/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StageDocumentResponse> generateScreenSpec(
            @PathVariable Long projectId,
            @Valid @RequestBody ScreenGenerateRequest request,
            @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(stageService.generateScreenSpec(projectId, request, loginUser));
    }

    @Operation(summary = "단계 문서 조회", description = "특정 프로젝트의 특정 단계 최신 문서를 조회한다.")
    @GetMapping("/projects/{projectId}/stage-documents/{stageType}")
    public ApiResponse<StageDocumentResponse> getStageDocument(
            @PathVariable Long projectId,
            @PathVariable StageType stageType,
            @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(stageService.getStageDocument(projectId, stageType, loginUser));
    }

    @Operation(summary = "문서 snapshot 저장", description = "공동편집으로 수정된 내용을 저장한다. CONFIRMED 상태에서는 수정 불가.")
    @PatchMapping("/stage-documents/{documentId}/snapshot")
    public ApiResponse<Void> updateSnapshot(
            @PathVariable Long documentId,
            @Valid @RequestBody SnapshotUpdateRequest request,
            @CurrentUser LoginUser loginUser) {
        stageService.updateSnapshot(documentId, request, loginUser);
        return ApiResponse.success();
    }

    @Operation(summary = "단계 확정", description = "리더가 현재 단계 문서를 확정한다. SCREEN_SPEC 확정 시 화면 목록이 자동 추출된다.")
    @PostMapping("/stage-documents/{documentId}/confirm")
    public ApiResponse<Void> confirmDocument(
            @PathVariable Long documentId,
            @CurrentUser LoginUser loginUser) {
        stageService.confirmDocument(documentId, loginUser);
        return ApiResponse.success();
    }
}