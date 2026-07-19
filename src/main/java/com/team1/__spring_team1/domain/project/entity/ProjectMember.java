package com.team1.__spring_team1.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "project_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_members_project_user",
                        columnNames = {"project_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_project_members_project_id", columnList = "project_id"),
                @Index(name = "idx_project_members_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private ProjectMemberRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public ProjectMember(Long projectId, Long userId, ProjectMemberRole role) {
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isLeader() {
        return this.role == ProjectMemberRole.LEADER;
    }

    public boolean isMember() {
        return this.role == ProjectMemberRole.MEMBER || this.role == ProjectMemberRole.LEADER;
    }
}