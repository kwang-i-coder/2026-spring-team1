package com.team1.__spring_team1.domain.wireframe.repository;

import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequest;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeRegenerationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WireframeRegenerationRequestRepository
        extends JpaRepository<WireframeRegenerationRequest, Long> {

    List<WireframeRegenerationRequest> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

    boolean existsByScreenIdAndStatus(
            Long screenId,
            WireframeRegenerationRequestStatus status
    );
}