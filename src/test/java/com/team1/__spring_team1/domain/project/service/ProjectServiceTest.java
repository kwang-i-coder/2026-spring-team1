package com.team1.__spring_team1.domain.project.service;

import com.team1.__spring_team1.domain.project.dto.request.ProjectCreateRequest;
import com.team1.__spring_team1.domain.project.dto.request.ProjectJoinRequest;
import com.team1.__spring_team1.domain.project.dto.response.ProjectCreateResponse;
import com.team1.__spring_team1.domain.project.dto.response.ProjectJoinResponse;
import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectInvite;
import com.team1.__spring_team1.domain.project.entity.ProjectMember;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;
import com.team1.__spring_team1.domain.project.repository.ProjectInviteRepository;
import com.team1.__spring_team1.domain.project.repository.ProjectMemberRepository;
import com.team1.__spring_team1.domain.project.repository.ProjectRepository;
import com.team1.__spring_team1.domain.user.repository.UserRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectInviteRepository projectInviteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                projectService,
                "frontendBaseUrl",
                "http://localhost:3000"
        );
    }

    @Test
    @DisplayName("프로젝트 생성 시 생성자를 LEADER로 저장한다")
    void createProject_savesCreatorAsLeader() {
        // given
        Long userId = 1L;

        ProjectCreateRequest request = new ProjectCreateRequest(
                "AI 기획 협업 도구",
                "회의록 기반 기획 서비스",
                "개발 전 요구사항 공백을 줄인다",
                LocalDate.of(2026, 7, 3),
                LocalDate.of(2026, 7, 20)
        );

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project project = invocation.getArgument(0);
                    ReflectionTestUtils.setField(project, "id", 1L);
                    return project;
                });

        when(projectMemberRepository.save(any(ProjectMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProjectCreateResponse response = projectService.createProject(userId, request);

        // then
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("AI 기획 협업 도구");
        assertThat(response.role()).isEqualTo(ProjectMemberRole.LEADER);
        assertThat(response.status()).isEqualTo(ProjectStatus.ACTIVE);

        ArgumentCaptor<ProjectMember> captor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(captor.capture());

        ProjectMember savedMember = captor.getValue();
        assertThat(savedMember.getProjectId()).isEqualTo(1L);
        assertThat(savedMember.getUserId()).isEqualTo(userId);
        assertThat(savedMember.getRole()).isEqualTo(ProjectMemberRole.LEADER);
    }

    @Test
    @DisplayName("리더는 프로젝트 초대 링크를 생성할 수 있다")
    void createInviteLink_success_whenLeader() {
        // given
        Long projectId = 1L;
        Long userId = 1L;

        Project project = createProject(projectId, userId);
        ProjectMember leader = new ProjectMember(projectId, userId, ProjectMemberRole.LEADER);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(leader));
        when(projectInviteRepository.existsByInviteToken(any(String.class))).thenReturn(false);
        when(projectInviteRepository.save(any(ProjectInvite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = projectService.createInviteLink(userId, projectId);

        // then
        assertThat(response.inviteToken()).isNotBlank();
        assertThat(response.inviteUrl()).contains("http://localhost:3000");
        assertThat(response.inviteUrl()).contains(response.inviteToken());
    }

    @Test
    @DisplayName("리더가 아닌 사용자는 초대 링크를 생성할 수 없다")
    void createInviteLink_throwsException_whenNotLeader() {
        // given
        Long projectId = 1L;
        Long userId = 2L;

        Project project = createProject(projectId, 1L);
        ProjectMember member = new ProjectMember(projectId, userId, ProjectMemberRole.MEMBER);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> projectService.createInviteLink(userId, projectId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_LEADER_ONLY);
    }

    @Test
    @DisplayName("만료된 초대 토큰으로는 프로젝트에 참여할 수 없다")
    void joinProject_throwsException_whenInviteExpired() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        String inviteToken = "expired-token";

        ProjectInvite expiredInvite = new ProjectInvite(
                projectId,
                inviteToken,
                1L,
                LocalDateTime.now().minusDays(1)
        );

        when(projectInviteRepository.findByInviteToken(inviteToken))
                .thenReturn(Optional.of(expiredInvite));

        // when & then
        assertThatThrownBy(() -> projectService.joinProject(
                userId,
                new ProjectJoinRequest(inviteToken)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVITE_EXPIRED);
    }

    @Test
    @DisplayName("이미 참여한 사용자는 같은 프로젝트에 다시 참여할 수 없다")
    void joinProject_throwsException_whenAlreadyMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        String inviteToken = "valid-token";

        ProjectInvite invite = new ProjectInvite(
                projectId,
                inviteToken,
                1L,
                LocalDateTime.now().plusDays(7)
        );

        Project project = createProject(projectId, 1L);

        when(projectInviteRepository.findByInviteToken(inviteToken))
                .thenReturn(Optional.of(invite));
        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> projectService.joinProject(
                userId,
                new ProjectJoinRequest(inviteToken)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_PROJECT_MEMBER);
    }

    @Test
    @DisplayName("유효한 초대 토큰으로 프로젝트에 참여하면 MEMBER로 저장된다")
    void joinProject_success_savesMember() {
        // given
        Long projectId = 1L;
        Long userId = 2L;
        String inviteToken = "valid-token";

        ProjectInvite invite = new ProjectInvite(
                projectId,
                inviteToken,
                1L,
                LocalDateTime.now().plusDays(7)
        );

        Project project = createProject(projectId, 1L);

        when(projectInviteRepository.findByInviteToken(inviteToken))
                .thenReturn(Optional.of(invite));
        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId))
                .thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ProjectJoinResponse response = projectService.joinProject(
                userId,
                new ProjectJoinRequest(inviteToken)
        );

        // then
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.title()).isEqualTo("AI 기획 협업 도구");
        assertThat(response.role()).isEqualTo(ProjectMemberRole.MEMBER);

        ArgumentCaptor<ProjectMember> captor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(captor.capture());

        ProjectMember savedMember = captor.getValue();
        assertThat(savedMember.getProjectId()).isEqualTo(projectId);
        assertThat(savedMember.getUserId()).isEqualTo(userId);
        assertThat(savedMember.getRole()).isEqualTo(ProjectMemberRole.MEMBER);
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