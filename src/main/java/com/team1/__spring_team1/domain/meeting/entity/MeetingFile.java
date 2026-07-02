package com.team1.__spring_team1.domain.meeting.entity;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.user.entity.User;
import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "meeting_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Lob
    @Column(name = "stored_file_url", nullable = false, columnDefinition = "TEXT")
    private String storedFileUrl;

    @Lob
    @Column(name = "stored_file_path", nullable = false, columnDefinition = "TEXT")
    private String storedFilePath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MeetingFileStatus status;

    @Lob
    @Column(name = "transcript", columnDefinition = "LONGTEXT")
    private String transcript;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    public MeetingFile(Project project, String title, String originalFileName,
                       String storedFileUrl, String storedFilePath,
                       String contentType, Long fileSize, User uploadedBy) {
        this.project = project;
        this.title = title;
        this.originalFileName = originalFileName;
        this.storedFileUrl = storedFileUrl;
        this.storedFilePath = storedFilePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.status = MeetingFileStatus.PENDING;
    }

    public void updateStatus(MeetingFileStatus status) {
        this.status = status;
    }

    public void completeTranscription(String transcript) {
        this.transcript = transcript;
        this.status = MeetingFileStatus.COMPLETED;
    }

    public void fail(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = MeetingFileStatus.FAILED;
    }
}