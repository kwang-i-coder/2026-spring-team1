package com.team1.__spring_team1.domain.wireframe.controller;

import com.team1.__spring_team1.domain.wireframe.dto.request.WireframeGenerateRequest;
import com.team1.__spring_team1.domain.wireframe.dto.request.WireframeRegenerationCreateRequest;
import com.team1.__spring_team1.domain.wireframe.dto.response.ScreenWireframeResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationCreateResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeRegenerationListResponse;
import com.team1.__spring_team1.domain.wireframe.service.WireframeRegenerationService;
import com.team1.__spring_team1.domain.wireframe.service.WireframeService;
import com.team1.__spring_team1.global.response.ApiResponse;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WireframeController {

    private final WireframeService wireframeService;
    private final WireframeRegenerationService wireframeRegenerationService;

    // 와이어프레임 생성
    @PostMapping("/projects/{projectId}/stages/wireframes/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<ScreenWireframeResponse>> generateWireframes(@PathVariable Long projectId, @Valid @RequestBody WireframeGenerateRequest request, @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(wireframeService.generateWireframe(projectId, request, loginUser));
    }

    // 프로젝트 화면 목록 조회
    @GetMapping("/projects/{projectId}/screens")
    public ApiResponse<List<ScreenWireframeResponse>> getScreens(@PathVariable Long projectId, @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(wireframeService.getScreens(projectId, loginUser));
    }

    // 화면별 와이어프레임 조회
    @GetMapping("/screens/{screenId}/wireframe")
    public ApiResponse<WireframeDslResponse> getWireframe(@PathVariable Long screenId, @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(wireframeService.getWireframe(screenId, loginUser));
    }

    // 와이어프레임 재생성 요청 등록
    @PostMapping("/screens/{screenId}/wireframe/regeneration-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WireframeRegenerationCreateResponse>
    createRegenerationRequest(@PathVariable Long screenId, @Valid @RequestBody WireframeRegenerationCreateRequest request, @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(wireframeRegenerationService.createRegenerationRequest(screenId, request, loginUser));
    }

    // 와이어프레임 재생성 요청 목록 조회
    @GetMapping("/projects/{projectId}/wireframe/regeneration-requests")
    public ApiResponse<WireframeRegenerationListResponse>
    getRegenerationRequests(@PathVariable Long projectId, @CurrentUser LoginUser loginUser) {
        return ApiResponse.success(wireframeRegenerationService.getRegenerationRequests(projectId, loginUser));
    }
}
