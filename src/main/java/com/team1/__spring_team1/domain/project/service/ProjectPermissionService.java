package com.team1.__spring_team1.domain.project.service;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMember;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.repository.ProjectMemberRepository;
import com.team1.__spring_team1.domain.project.repository.ProjectRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 권한 검증 전용 서비스
 *
 * 다른 도메인(Meeting, Stage, Wireframe 등)에서
 * 프로젝트 존재 여부, 프로젝트 멤버 여부, 리더 권한 여부를
 * 공통으로 검증할 때 사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectPermissionService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    /**
     * 프로젝트 존재 여부를 검증한다.
     *
     * projectId에 해당하는 프로젝트가 존재하면 Project를 반환하고,
     * 존재하지 않으면 PROJECT_NOT_FOUND 예외를 발생시킨다.
     *
     * @param projectId 검증할 프로젝트 ID
     * @return 조회된 Project Entity
     */
    public Project validateProjectExists(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    /**
     * 사용자가 해당 프로젝트의 멤버인지 검증한다.
     *
     * 먼저 프로젝트가 존재하는지 확인한 뒤,
     * project_members 테이블에 projectId와 userId 조합이 존재하는지 검사한다.
     *
     * 사용자가 프로젝트에 참여하지 않은 경우 NOT_PROJECT_MEMBER 예외를 발생시킨다.
     *
     * @param projectId 검증할 프로젝트 ID
     * @param userId 검증할 사용자 ID
     */
    public void validateProjectMember(Long projectId, Long userId) {
        validateProjectExists(projectId);

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    /**
     * 사용자가 해당 프로젝트의 리더인지 검증한다.
     *
     * 먼저 프로젝트가 존재하는지 확인한 뒤,
     * 해당 사용자가 프로젝트 멤버인지 조회한다.
     *
     * 프로젝트 멤버가 아니면 NOT_PROJECT_MEMBER 예외를 발생시키고,
     * 멤버이지만 role이 LEADER가 아니면 PROJECT_LEADER_ONLY 예외를 발생시킨다.
     *
     * @param projectId 검증할 프로젝트 ID
     * @param userId 검증할 사용자 ID
     */
    public void validateProjectLeader(Long projectId, Long userId) {
        validateProjectExists(projectId);

        ProjectMember projectMember = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PROJECT_MEMBER));

        if (!projectMember.isLeader()) {
            throw new BusinessException(ErrorCode.PROJECT_LEADER_ONLY);
        }
    }

    /**
     * 사용자의 프로젝트 내 역할을 조회한다.
     *
     * 먼저 프로젝트가 존재하는지 확인한 뒤,
     * 해당 사용자의 ProjectMember 정보를 조회하여 role을 반환한다.
     *
     * 프로젝트 멤버가 아닌 경우 NOT_PROJECT_MEMBER 예외를 발생시킨다.
     *
     * 주로 다른 도메인에서 현재 사용자의 role에 따라
     * 화면 응답이나 비즈니스 로직을 분기할 때 사용할 수 있다.
     *
     * @param projectId 조회할 프로젝트 ID
     * @param userId 조회할 사용자 ID
     * @return 프로젝트 내 사용자 역할, LEADER 또는 MEMBER
     */
    public ProjectMemberRole getProjectMemberRole(Long projectId, Long userId) {
        validateProjectExists(projectId);

        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PROJECT_MEMBER))
                .getRole();
    }
}