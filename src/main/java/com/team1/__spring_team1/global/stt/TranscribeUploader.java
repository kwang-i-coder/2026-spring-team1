package com.team1.__spring_team1.global.stt;

import com.team1.__spring_team1.global.s3.S3Directory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.LanguageCode;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;

@Component
@RequiredArgsConstructor
public class TranscribeUploader {

    private static final String JOB_NAME_PREFIX = "meeting-file-";

    private final TranscribeClient transcribeClient;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void startTranscriptionJob(Long meetingFileId, String mediaFileUri) {
        String jobName = toJobName(meetingFileId);
        String outputKey = String.format("%s/%s.json", S3Directory.TRANSCRIBE_RESULT.getPath(), jobName);

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(LanguageCode.KO_KR)
                .media(Media.builder().mediaFileUri(mediaFileUri).build())
                .outputKey(outputKey)
                .outputBucketName(bucket)
                .build();

        transcribeClient.startTranscriptionJob(request);
    }

    public static String toJobName(Long meetingFileId) {
        return JOB_NAME_PREFIX + meetingFileId;
    }
}