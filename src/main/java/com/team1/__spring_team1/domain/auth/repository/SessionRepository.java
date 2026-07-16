package com.team1.__spring_team1.domain.auth.repository;

import com.team1.__spring_team1.domain.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("""
        select s
        from Session s
        join fetch s.user
        where s.sessionTokenHash = :sessionTokenHash
    """)
    Optional<Session> findBySessionTokenHash(
            @Param("sessionTokenHash") String sessionTokenHash
    );
}