package com.team1.__spring_team1.domain.project.service;

import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectMember;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.repository.ProjectMemberRepository;
import com.team1.__spring_team1.domain.project.repository.ProjectRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPermissionServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectPermissionService projectPermissionService;

    @Test
    @DisplayName("프로젝트가 없으면 PROJECT_NOT_FOUND 예외가 발생한다")
    void validateProjectExists_throwsException_whenProjectNotFound() {
        // given
        Long projectId = 999L;

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectPermissionService.validateProjectExists(projectId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND)
                );
    }

    @Test
    @DisplayName("프로젝트 멤버가 아니면 NOT_PROJECT_MEMBER 예외가 발생한다")
    void validateProjectMember_throwsException_whenUserIsNotProjectMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        Project project = createProject(projectId, 1L);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> projectPermissionService.validateProjectMember(projectId, userId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_PROJECT_MEMBER)
                );
    }

    @Test
    @DisplayName("프로젝트 멤버이면 검증을 통과한다")
    void validateProjectMember_success_whenUserIsProjectMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        Project project = createProject(projectId, 1L);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId))
                .thenReturn(true);

        // when & then
        assertThatCode(() -> projectPermissionService.validateProjectMember(projectId, userId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("프로젝트 멤버이지만 리더가 아니면 PROJECT_LEADER_ONLY 예외가 발생한다")
    void validateProjectLeader_throwsException_whenUserIsNotLeader() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        Project project = createProject(projectId, 1L);
        ProjectMember member = new ProjectMember(projectId, userId, ProjectMemberRole.MEMBER);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> projectPermissionService.validateProjectLeader(projectId, userId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_LEADER_ONLY)
                );
    }

    @Test
    @DisplayName("프로젝트 리더이면 검증을 통과한다")
    void validateProjectLeader_success_whenUserIsLeader() {
        // given
        Long projectId = 1L;
        Long userId = 1L;

        Project project = createProject(projectId, userId);
        ProjectMember leader = new ProjectMember(projectId, userId, ProjectMemberRole.LEADER);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(leader));

        // when & then
        assertThatCode(() -> projectPermissionService.validateProjectLeader(projectId, userId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("프로젝트 멤버 역할을 조회한다")
    void getProjectMemberRole_success() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        Project project = createProject(projectId, 1L);
        ProjectMember member = new ProjectMember(projectId, userId, ProjectMemberRole.MEMBER);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(member));

        // when
        ProjectMemberRole role = projectPermissionService.getProjectMemberRole(projectId, userId);

        // then
        assertThat(role).isEqualTo(ProjectMemberRole.MEMBER);
    }

    private Project createProject(Long projectId, Long createdBy) {
        Project project = new Project(
                "AI 기획 협업 도구",
                "회의록 기반 기획 서비스",
                "개발 전 요구사항 공백을 줄인다",
                LocalDate.of(2026, 7, 3),
                LocalDate.of(2026, 7, 20),
                createdBy
        );

        ReflectionTestUtils.setField(project, "id", projectId);

        return project;
    }
}