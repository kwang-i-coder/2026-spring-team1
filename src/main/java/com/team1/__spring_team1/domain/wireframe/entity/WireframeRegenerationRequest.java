package com.team1.__spring_team1.domain.wireframe.entity;

import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wireframe_regeneration_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WireframeRegenerationRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "screen_id",nullable = false)
    private Long screenId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "reason",columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WireframeRegenerationRequestStatus status;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public WireframeRegenerationRequest(
            Long projectId,
            Long screenId,
            Long requesterId,
            String reason
    ) {
        this.projectId = projectId;
        this.screenId = screenId;
        this.requesterId = requesterId;
        this.reason = reason;
        this.status = WireframeRegenerationRequestStatus.PENDING;
    }

    public void approve(Long reviewerId) {
        validatePending();
        this.status = WireframeRegenerationRequestStatus.APPROVED;
        this.reviewerId = reviewerId;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(Long reviewerId) {
        validatePending();
        this.status = WireframeRegenerationRequestStatus.REJECTED;
        this.reviewerId = reviewerId;
        this.reviewedAt = LocalDateTime.now();
    }

    private void validatePending() {
        if (this.status != WireframeRegenerationRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.REGENERATION_REQUEST_ALREADY_HANDLED);
        }
    }
}