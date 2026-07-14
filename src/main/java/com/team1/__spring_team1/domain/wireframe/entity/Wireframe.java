package com.team1.__spring_team1.domain.wireframe.entity;

import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wireframe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wireframe extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "screen_id", nullable = false)
    private Long screenId;

    @Column(name="wireframe_json", columnDefinition = "TEXT", nullable = false)
    private String jsonDsl;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false, length = 20)
    private WireframeStatus status;

    @Column(name = "version", nullable = false)
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