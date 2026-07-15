package com.team1.__spring_team1.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    //directory: s3 prefix. 우선은 "meeting-file"로 세팅
    public S3UploadResult upload(MultipartFile file, S3Directory  directory) {
        String key = createKey(file, directory);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new UncheckedIOException("S3 파일 업로드 실패", e);
        }

        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        return new S3UploadResult(key, url);
    }

    //서로 다른 유저임에도 파일 이름 같을 경우 덮어씌워지는 이슈 발생. UUID로 전환 로직
    private String createKey(MultipartFile file, S3Directory directory) {
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        String datePath = LocalDate.now().toString().replace("-", "/"); // 2026/07/12

        return String.format("%s/%s/%s", directory.getPath(), datePath, fileName);
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf("."));
    }
}