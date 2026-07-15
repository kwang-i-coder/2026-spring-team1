package com.team1.__spring_team1.domain.project.repository;

import com.team1.__spring_team1.domain.project.entity.ProjectInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectInviteRepository extends JpaRepository<ProjectInvite, Long> {

    Optional<ProjectInvite> findByInviteToken(String inviteToken);

    boolean existsByInviteToken(String inviteToken);
}