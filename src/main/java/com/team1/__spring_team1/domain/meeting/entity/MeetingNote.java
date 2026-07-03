package com.team1.__spring_team1.domain.meeting.entity;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.user.entity.User;
import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "meeting_notes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingNote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public MeetingNote(Long projectId, String title, String content, Long createdBy) {
        this.projectId = projectId;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
    }
}
