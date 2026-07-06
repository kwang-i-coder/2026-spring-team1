package com.team1.__spring_team1.domain.user.repository;

import com.team1.__spring_team1.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

// 프로젝트 멤버 목록에서 사용자 이름/loginId 조회를 위해 UserRepository 추가
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIdIn(Collection<Long> ids);
}