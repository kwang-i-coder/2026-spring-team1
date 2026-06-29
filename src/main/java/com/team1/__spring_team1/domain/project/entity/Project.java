package com.team1.__spring_team1.domain.project.entity;

import com.team1.__spring_team1.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "projects",
        indexes = {
                @Index(name = "idx_projects_created_by", columnList = "created_by"),
                @Index(name = "idx_projects_updated_at", columnList = "updated_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 30)
    private ProjectStage currentStage;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public Project(
            String title,
            String description,
            String goal,
            LocalDate startDate,
            LocalDate endDate,
            Long createdBy
    ) {
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.status = ProjectStatus.ACTIVE;
        this.currentStage = ProjectStage.MEETING;
    }

    public void updateCurrentStage(ProjectStage currentStage) {
        this.currentStage = currentStage;
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }
}