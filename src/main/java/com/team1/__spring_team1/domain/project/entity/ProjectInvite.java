package com.team1.__spring_team1.domain.project.entity;

import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "project_invites",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_invites_invite_token",
                        columnNames = "invite_token"
                )
        },
        indexes = {
                @Index(name = "idx_project_invites_project_id", columnList = "project_id"),
                @Index(name = "idx_project_invites_created_by", columnList = "created_by"),
                @Index(name = "idx_project_invites_expires_at", columnList = "expires_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectInvite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "invite_token", nullable = false, unique = true, length = 100)
    private String inviteToken;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public ProjectInvite(
            Long projectId,
            String inviteToken,
            Long createdBy,
            LocalDateTime expiresAt
    ) {
        this.projectId = projectId;
        this.inviteToken = inviteToken;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}