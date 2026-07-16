package com.team1.__spring_team1.meeting;

import com.team1.__spring_team1.global.s3.S3Directory;
import com.team1.__spring_team1.global.s3.S3UploadResult;
import com.team1.__spring_team1.global.s3.S3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3UploaderTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Uploader s3Uploader;

    @Test
    @DisplayName("파일 업로드 시 S3Client의 putObject가 호출되고, key와 url을 반환한다")
    void upload_success() {
        ReflectionTestUtils.setField(s3Uploader, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3Uploader, "region", "ap-northeast-2");

        MockMultipartFile file = new MockMultipartFile(
                "file", "meeting-record.mp3", "audio/mpeg", "fake audio content".getBytes()
        );

        S3UploadResult result = s3Uploader.upload(file, S3Directory.MEETING_FILE);

        assertThat(result.key()).startsWith("meeting-file/");   // 소문자 + 하이픈
        assertThat(result.key()).endsWith(".mp3");
        assertThat(result.url()).contains("test-bucket");
        assertThat(result.url()).contains("meeting-file/");

        verify(s3Client, times(1))
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("확장자가 없는 파일도 예외 없이 업로드된다")
    void upload_withoutExtension() {
        ReflectionTestUtils.setField(s3Uploader, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3Uploader, "region", "ap-northeast-2");

        MockMultipartFile file = new MockMultipartFile(
                "file", "recording", "audio/mpeg", "content".getBytes()
        );

        S3UploadResult result = s3Uploader.upload(file, S3Directory.MEETING_FILE);

        assertThat(result.key()).startsWith("meeting-file/");   // 소문자 + 하이픈

        verify(s3Client, times(1))
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}