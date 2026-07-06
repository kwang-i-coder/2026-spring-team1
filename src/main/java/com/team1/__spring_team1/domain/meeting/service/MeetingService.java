package com.team1.__spring_team1.domain.meeting.service;

import com.team1.__spring_team1.domain.meeting.dto.MeetingFileStatusResponse;
import com.team1.__spring_team1.domain.meeting.dto.MeetingFileUploadResponse;
import com.team1.__spring_team1.domain.meeting.dto.MeetingNoteCreateRequest;
import com.team1.__spring_team1.domain.meeting.dto.MeetingNoteCreateResponse;
import com.team1.__spring_team1.domain.meeting.entity.MeetingFile;
import com.team1.__spring_team1.domain.meeting.entity.MeetingNote;
import com.team1.__spring_team1.domain.meeting.repository.MeetingFileRepository;
import com.team1.__spring_team1.domain.meeting.repository.MeetingNoteRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingNoteRepository meetingNoteRepository;
    private final MeetingFileRepository meetingFileRepository;
    //private final ProjectRepository projectRepository;
    //private final ProjectMemberRepository projectMemberRepository;
    // private final FileStorageService fileStorageService; // TODO: S3 인프라 준비되면 주입

    @Transactional
    public MeetingNoteCreateResponse createMeetingNote(
            Long projectId,
            Long userId,
            MeetingNoteCreateRequest request
    ) {
        validateProjectExists(projectId);
        validateProjectMember(projectId, userId);

        MeetingNote meetingNote = new MeetingNote(
                projectId,
                request.getTitle(),
                request.getContent(),
                userId
        );
        meetingNoteRepository.save(meetingNote);

        return new MeetingNoteCreateResponse(meetingNote.getId(), meetingNote.getTitle());
    }

    private void validateProjectExists(Long projectId) {
        // TODO: projectRepo 병합 시 if (!projectRepository.existsById(projectId))로 교체
        if (false) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
    }

    private void validateProjectMember(Long projectId, Long userId) {
        // TODO: projectRepo 병합 시 true => projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);로 교체
        boolean isMember = true;
        if (!isMember) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    @Transactional
    public MeetingFileUploadResponse uploadMeetingFile(
            Long projectId,
            Long userId,
            MultipartFile file
    ) {
        validateProjectExists(projectId);
        validateProjectMember(projectId, userId);

        // TODO: S3 연동 후 실제 업로드 결과(url, key)로 교체
        String storedFileUrl = "TODO";
        String storedFilePath = "TODO";

        MeetingFile meetingFile = new MeetingFile(
                projectId,
                file.getOriginalFilename(),
                file.getOriginalFilename(),
                storedFileUrl,
                storedFilePath,
                file.getContentType(),
                file.getSize(),
                userId
        );
        meetingFileRepository.save(meetingFile);

        // TODO: STT 비동기 작업 트리거

        return new MeetingFileUploadResponse(
                meetingFile.getId(),
                meetingFile.getOriginalFileName(),
                meetingFile.getStatus()
        );
    }

    public MeetingFileStatusResponse getMeetingFileStatus(Long fileId, Long userId) {
        MeetingFile meetingFile = meetingFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_FILE_NOT_FOUND));

        validateProjectMember(meetingFile.getProjectId(), userId);

        return new MeetingFileStatusResponse(
                meetingFile.getId(),
                meetingFile.getStatus(),
                meetingFile.getTranscript()
        );
    }
}