package com.team1.__spring_team1.domain.wireframe.entity;

import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WireframeRegenerationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long screenId;

    @Column(nullable = false)
    private Long requesterId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WireframeRegenerationRequestStatus status;

    private Long reviewerId;

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