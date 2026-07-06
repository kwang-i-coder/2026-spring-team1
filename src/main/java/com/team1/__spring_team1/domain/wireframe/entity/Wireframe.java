package com.team1.__spring_team1.domain.wireframe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wireframe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * TO-DO
     * 나중에 Project Entity가 완료하면 연관관계로 바꿈
     */
    @Column(nullable = false)
    private Long projectId;

    /**
     * Screen 도메인으로.
     */
    @Column(nullable = false)
    private Long screenId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String jsonDsl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WireframeStatus status;

    @Column(nullable = false)
    private Integer version;

    public Wireframe(Long projectId, Long screenId, String jsonDsl) {
        this.projectId = projectId;
        this.screenId = screenId;
        this.jsonDsl = jsonDsl;
        this.status = WireframeStatus.COMPLETED;
        this.version = 1;
    }

    public void regenerate(String jsonDsl) {
        this.jsonDsl = jsonDsl;
        this.status = WireframeStatus.COMPLETED;
        this.version += 1;
    }

    public void markFailed() {
        this.status = WireframeStatus.FAILED;
    }
}