package com.team1.__spring_team1.global.stt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.meeting.dto.TranscribeResultResponse;
import com.team1.__spring_team1.global.s3.S3Directory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TranscribeStatusChecker {

    private final TranscribeClient transcribeClient;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public TranscriptionJob getJobStatus(Long meetingFileId) {
        String jobName = TranscribeUploader.toJobName(meetingFileId);

        GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .build();

        return transcribeClient.getTranscriptionJob(request).transcriptionJob();
    }

    public boolean isCompleted(TranscriptionJob job) {
        return job.transcriptionJobStatus() == TranscriptionJobStatus.COMPLETED;
    }

    public boolean isFailed(TranscriptionJob job) {
        return job.transcriptionJobStatus() == TranscriptionJobStatus.FAILED;
    }

    public String readTranscriptText(Long meetingFileId) {
        String key = String.format("%s/%s.json",
                S3Directory.TRANSCRIBE_RESULT.getPath(),
                TranscribeUploader.toJobName(meetingFileId));

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            TranscribeResultResponse result = objectMapper.readValue(response, TranscribeResultResponse.class);
            return result.extractTranscript();
        } catch (IOException e) {
            throw new RuntimeException("STT 결과 파일 읽기 실패", e);
        }
    }
}