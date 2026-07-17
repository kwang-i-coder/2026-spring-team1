package com.team1.__spring_team1.meeting;

import com.team1.__spring_team1.domain.meeting.entity.MeetingFile;
import com.team1.__spring_team1.domain.meeting.entity.MeetingFileStatus;
import com.team1.__spring_team1.domain.meeting.repository.MeetingFileRepository;
import com.team1.__spring_team1.global.stt.TranscribeStatusChecker;
import com.team1.__spring_team1.global.stt.TranscriptionScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranscriptionSchedulerTest {

    @Mock
    private MeetingFileRepository meetingFileRepository;

    @Mock
    private TranscribeStatusChecker transcribeStatusChecker;

    @InjectMocks
    private TranscriptionScheduler transcriptionScheduler;

    @Test
    @DisplayName("STT가 완료된 파일은 transcript를 채우고 COMPLETED 상태로 바꾼다")
    void checkTranscriptionJobs_completed() {
        // given
        MeetingFile meetingFile = createMeetingFile();
        when(meetingFileRepository.findAllByStatus(MeetingFileStatus.TRANSCRIBING))
                .thenReturn(List.of(meetingFile));

        TranscriptionJob job = TranscriptionJob.builder()
                .transcriptionJobStatus(TranscriptionJobStatus.COMPLETED)
                .build();
        when(transcribeStatusChecker.getJobStatus(anyLong())).thenReturn(job);
        when(transcribeStatusChecker.isCompleted(job)).thenReturn(true);
        when(transcribeStatusChecker.readTranscriptText(anyLong())).thenReturn("변환된 텍스트");

        // when
        transcriptionScheduler.checkTranscriptionJobs();

        // then
        assertThat(meetingFile.getStatus()).isEqualTo(MeetingFileStatus.COMPLETED);
        assertThat(meetingFile.getTranscript()).isEqualTo("변환된 텍스트");
    }

    @Test
    @DisplayName("STT가 실패한 파일은 FAILED 상태로 바꾼다")
    void checkTranscriptionJobs_failed() {
        MeetingFile meetingFile = createMeetingFile();
        when(meetingFileRepository.findAllByStatus(MeetingFileStatus.TRANSCRIBING))
                .thenReturn(List.of(meetingFile));

        TranscriptionJob job = TranscriptionJob.builder()
                .transcriptionJobStatus(TranscriptionJobStatus.FAILED)
                .build();
        when(transcribeStatusChecker.getJobStatus(anyLong())).thenReturn(job);
        when(transcribeStatusChecker.isCompleted(job)).thenReturn(false);
        when(transcribeStatusChecker.isFailed(job)).thenReturn(true);

        transcriptionScheduler.checkTranscriptionJobs();

        assertThat(meetingFile.getStatus()).isEqualTo(MeetingFileStatus.FAILED);
        assertThat(meetingFile.getErrorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("아직 진행 중인 파일은 상태를 그대로 둔다")
    void checkTranscriptionJobs_inProgress() {
        MeetingFile meetingFile = createMeetingFile();
        when(meetingFileRepository.findAllByStatus(MeetingFileStatus.TRANSCRIBING))
                .thenReturn(List.of(meetingFile));

        TranscriptionJob job = TranscriptionJob.builder()
                .transcriptionJobStatus(TranscriptionJobStatus.IN_PROGRESS)
                .build();
        when(transcribeStatusChecker.getJobStatus(anyLong())).thenReturn(job);
        when(transcribeStatusChecker.isCompleted(job)).thenReturn(false);
        when(transcribeStatusChecker.isFailed(job)).thenReturn(false);

        transcriptionScheduler.checkTranscriptionJobs();

        assertThat(meetingFile.getStatus()).isEqualTo(MeetingFileStatus.TRANSCRIBING);
        assertThat(meetingFile.getTranscript()).isNull();
    }

    @Test
    @DisplayName("TRANSCRIBING 상태 파일이 없으면 STT 조회 자체가 일어나지 않는다")
    void checkTranscriptionJobs_noFiles() {
        when(meetingFileRepository.findAllByStatus(MeetingFileStatus.TRANSCRIBING))
                .thenReturn(List.of());

        transcriptionScheduler.checkTranscriptionJobs();

        org.mockito.Mockito.verifyNoInteractions(transcribeStatusChecker);
    }

    private MeetingFile createMeetingFile() {
        MeetingFile meetingFile = new MeetingFile(
                1L, "회의 파일", "recording.mp3",
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/meeting-file/uuid.mp3",
                "meeting-file/uuid.mp3",
                "audio/mpeg", 1000L, 1L
        );
        org.springframework.test.util.ReflectionTestUtils.setField(meetingFile, "id", 1L);
        return meetingFile;
    }
}