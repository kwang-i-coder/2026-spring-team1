package com.team1.__spring_team1.domain.meeting.controller;

import com.team1.__spring_team1.domain.meeting.dto.MeetingFileStatusResponse;
import com.team1.__spring_team1.domain.meeting.dto.MeetingFileUploadResponse;
import com.team1.__spring_team1.domain.meeting.dto.MeetingNoteCreateRequest;
import com.team1.__spring_team1.domain.meeting.dto.MeetingNoteCreateResponse;
import com.team1.__spring_team1.domain.meeting.service.MeetingService;
import com.team1.__spring_team1.global.response.ApiResponse;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    // 회의록 텍스트 업로드
    @PostMapping("/projects/{projectId}/meeting-notes")
    public ResponseEntity<ApiResponse<MeetingNoteCreateResponse>> createMeetingNote(
            @PathVariable Long projectId,
            @CurrentUser LoginUser loginUser,
            @Valid @RequestBody MeetingNoteCreateRequest request
    ) {
        MeetingNoteCreateResponse response = meetingService.createMeetingNote(
                projectId,
                loginUser.userId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 회의 녹음 파일 업로드
    @PostMapping(value = "/projects/{projectId}/meeting-files", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<MeetingFileUploadResponse>> uploadMeetingFile(
            @PathVariable Long projectId,
            @CurrentUser LoginUser loginUser,
            @RequestParam("file") MultipartFile file
    ) {
        MeetingFileUploadResponse response = meetingService.uploadMeetingFile(
                projectId,
                loginUser.userId(),
                file
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 회의 파일 상태 조회
    @GetMapping("/meeting-files/{fileId}")
    public ResponseEntity<ApiResponse<MeetingFileStatusResponse>> getMeetingFileStatus(
            @PathVariable Long fileId,
            @CurrentUser LoginUser loginUser
    ) {
        MeetingFileStatusResponse response = meetingService.getMeetingFileStatus(
                fileId,
                loginUser.userId()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}