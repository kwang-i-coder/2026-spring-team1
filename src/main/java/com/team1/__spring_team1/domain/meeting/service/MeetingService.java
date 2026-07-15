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
import com.team1.__spring_team1.global.s3.S3Directory;
import com.team1.__spring_team1.global.s3.S3UploadResult;
import com.team1.__spring_team1.global.s3.S3Uploader;
import com.team1.__spring_team1.global.stt.TranscribeUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.team1.__spring_team1.global.s3.S3Directory.MEETING_FILE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

    private final MeetingNoteRepository meetingNoteRepository;
    private final MeetingFileRepository meetingFileRepository;
    //private final ProjectRepository projectRepository;
    //private final ProjectMemberRepository projectMemberRepository;
    private final S3Uploader s3Uploader;
    private final TranscribeUploader transcribeUploader;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

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
        //유저 검증
        validateProjectExists(projectId);
        validateProjectMember(projectId, userId);

        //S3에 파일 전송. url, key 수신.
        S3UploadResult uploadResult = uploadToS3(file);

        String storedFileUrl = uploadResult.url();
        String storedFilePath = uploadResult.key();

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

        //DB에 MeetingFile 저장
        meetingFileRepository.save(meetingFile);

        //STT 비동기 작업 트리거. -> transcribe - 백엔드 내에서 지속적 풀링으로 완료 상태 확인.
        // 완료 시 백엔드 - 프론트 풀링을 통해 프론트에 완료 및 변환 파일 제공.
        startTranscription(meetingFile, uploadResult);

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

    private S3UploadResult uploadToS3(MultipartFile file) {
        try {
            return s3Uploader.upload(file, MEETING_FILE);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void startTranscription(MeetingFile meetingFile, S3UploadResult uploadResult) {
        String mediaFileUri = String.format("s3://%s/%s", bucket, uploadResult.key());
        try {
            transcribeUploader.startTranscriptionJob(meetingFile.getId(), mediaFileUri);
        } catch (Exception e) {
            meetingFile.fail("STT 변환 시작에 실패했습니다.");
        }
    }
}