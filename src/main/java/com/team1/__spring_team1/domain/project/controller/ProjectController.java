package com.team1.__spring_team1.domain.project.controller;

import com.team1.__spring_team1.domain.project.dto.request.ProjectCreateRequest;
import com.team1.__spring_team1.domain.project.dto.request.ProjectJoinRequest;
import com.team1.__spring_team1.domain.project.dto.response.*;
import com.team1.__spring_team1.domain.project.service.ProjectService;
import com.team1.__spring_team1.global.response.ApiResponse;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectCreateResponse>> createProject(
            @Parameter(hidden = true) @CurrentUser LoginUser loginUser,
            @RequestBody ProjectCreateRequest request
    ) {
        ProjectCreateResponse response = projectService.createProject(
                loginUser.userId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ApiResponse<ProjectListResponse> getMyProjects(
            @Parameter(hidden = true) @CurrentUser LoginUser loginUser
    ) {
        return ApiResponse.success(
                projectService.getMyProjects(loginUser.userId())
        );
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getProjectDetail(
            @Parameter(hidden = true) @CurrentUser LoginUser loginUser,
            @PathVariable Long projectId
    ) {
        return ApiResponse.success(
                projectService.getProjectDetail(loginUser.userId(), projectId)
        );
    }

    @GetMapping("/{projectId}/members")
    public ApiResponse<ProjectMemberListResponse> getProjectMembers(
            @Parameter(hidden = true) @CurrentUser LoginUser loginUser,
            @PathVariable Long projectId
    ) {
        return ApiResponse.success(
                projectService.getProjectMembers(loginUser.userId(), projectId)
        );
    }

    @PostMapping("/{projectId}/invite-link")
    public ResponseEntity<ApiResponse<ProjectInviteLinkResponse>> createInviteLink(
            @Parameter(hidden = true) @CurrentUser LoginUser loginUser,
            @PathVariable Long projectId
    ) {
        ProjectInviteLinkResponse response = projectService.createInviteLink(
                loginUser.userId(),
                projectId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/join")
    public ApiResponse<ProjectJoinResponse> joinProject(
            @Parameter(hidden = true) @CurrentUser LoginUser loginUser,
            @RequestBody ProjectJoinRequest request
    ) {
        return ApiResponse.success(
                projectService.joinProject(loginUser.userId(), request)
        );
    }
}