package com.team1.__spring_team1.domain.wireframe.repository;

import com.team1.__spring_team1.domain.wireframe.entity.Wireframe;
import org.springframework.data.jpa.repository.JpaRepository;
import com.team1.__spring_team1.domain.wireframe.entity.WireframeStatus;

import java.util.List;
import java.util.Optional;

public interface WireframeRepository extends JpaRepository<Wireframe, Long> {

    Optional<Wireframe> findByScreenId(Long screenId);

    List<Wireframe> findAllByProjectId(Long projectId);

    List<Wireframe> findAllByProjectIdAndStatus(Long projectId, WireframeStatus status);

    boolean existsByScreenId(Long screenId);
}