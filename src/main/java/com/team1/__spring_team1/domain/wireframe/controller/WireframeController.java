package com.team1.__spring_team1.domain.wireframe.controller;

import com.team1.__spring_team1.domain.wireframe.dto.response.ScreenWireframeResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.service.WireframeService;
import com.team1.__spring_team1.global.response.ApiResponse;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WireframeController {

    private final WireframeService wireframeService;

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
}
