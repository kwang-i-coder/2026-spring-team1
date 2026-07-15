package com.team1.__spring_team1.meeting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.global.stt.TranscribeStatusChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobResponse;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranscribeStatusCheckerTest {

    @Mock
    private TranscribeClient transcribeClient;

    @Mock
    private S3Client s3Client;

    private TranscribeStatusChecker checker;

    @BeforeEach
    void setUp() {
        checker = new TranscribeStatusChecker(transcribeClient, s3Client, new ObjectMapper());
        ReflectionTestUtils.setField(checker, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("getJobStatus는 meetingFileId로 계산한 jobName으로 Transcribe에 상태를 조회한다")
    void getJobStatus_success() {
        TranscriptionJob job = TranscriptionJob.builder()
                .transcriptionJobName("meeting-file-1")
                .transcriptionJobStatus(TranscriptionJobStatus.IN_PROGRESS)
                .build();

        when(transcribeClient.getTranscriptionJob(any(GetTranscriptionJobRequest.class)))
                .thenReturn(GetTranscriptionJobResponse.builder().transcriptionJob(job).build());

        TranscriptionJob result = checker.getJobStatus(1L);

        assertThat(result.transcriptionJobStatus()).isEqualTo(TranscriptionJobStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("상태가 COMPLETED면 isCompleted는 true를 반환한다")
    void isCompleted_true() {
        TranscriptionJob job = TranscriptionJob.builder()
                .transcriptionJobStatus(TranscriptionJobStatus.COMPLETED)
                .build();

        assertThat(checker.isCompleted(job)).isTrue();
        assertThat(checker.isFailed(job)).isFalse();
    }

    @Test
    @DisplayName("상태가 FAILED면 isFailed는 true를 반환한다")
    void isFailed_true() {
        TranscriptionJob job = TranscriptionJob.builder()
                .transcriptionJobStatus(TranscriptionJobStatus.FAILED)
                .build();

        assertThat(checker.isFailed(job)).isTrue();
        assertThat(checker.isCompleted(job)).isFalse();
    }

    @Test
    @DisplayName("상태가 IN_PROGRESS면 completed도 failed도 아니다")
    void isInProgress() {
        TranscriptionJob job = TranscriptionJob.builder()
                .transcriptionJobStatus(TranscriptionJobStatus.IN_PROGRESS)
                .build();

        assertThat(checker.isCompleted(job)).isFalse();
        assertThat(checker.isFailed(job)).isFalse();
    }

    @Test
    @DisplayName("readTranscriptText는 S3의 결과 JSON에서 transcript 텍스트만 추출한다")
    void readTranscriptText_success() {
        String json = """
                {
                  "results": {
                    "transcripts": [
                      { "transcript": "회의록 변환 결과 텍스트입니다." }
                    ]
                  }
                }
                """;

        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        String transcript = checker.readTranscriptText(1L);

        assertThat(transcript).isEqualTo("회의록 변환 결과 텍스트입니다.");
    }
}