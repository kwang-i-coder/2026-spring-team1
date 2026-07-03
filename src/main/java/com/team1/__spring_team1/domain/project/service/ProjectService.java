package com.team1.__spring_team1.domain.project.service;

import com.team1.__spring_team1.domain.project.dto.request.ProjectCreateRequest;
import com.team1.__spring_team1.domain.project.dto.request.ProjectJoinRequest;
import com.team1.__spring_team1.domain.project.dto.response.ProjectCreateResponse;
import com.team1.__spring_team1.domain.project.dto.response.ProjectDetailResponse;
import com.team1.__spring_team1.domain.project.dto.response.ProjectInviteLinkResponse;
import com.team1.__spring_team1.domain.project.dto.response.ProjectJoinResponse;
import com.team1.__spring_team1.domain.project.dto.response.ProjectListResponse;
import com.team1.__spring_team1.domain.project.dto.response.ProjectMemberResponse;
import com.team1.__spring_team1.domain.project.entity.Project;
import com.team1.__spring_team1.domain.project.entity.ProjectInvite;
import com.team1.__spring_team1.domain.project.entity.ProjectMember;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;
import com.team1.__spring_team1.domain.project.repository.ProjectInviteRepository;
import com.team1.__spring_team1.domain.project.repository.ProjectMemberRepository;
import com.team1.__spring_team1.domain.project.repository.ProjectRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private static final int INVITE_EXPIRE_DAYS = 7;

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInviteRepository projectInviteRepository;

    @Transactional
    public ProjectCreateResponse createProject(Long userId, ProjectCreateRequest request) {
        Project project = new Project(
                request.title(),
                request.description(),
                request.goal(),
                request.startDate(),
                request.endDate(),
                userId
        );

        Project savedProject = projectRepository.save(project);

        ProjectMember leader = new ProjectMember(
                savedProject.getId(),
                userId,
                ProjectMemberRole.LEADER
        );

        projectMemberRepository.save(leader);

        return ProjectCreateResponse.from(savedProject, ProjectMemberRole.LEADER);
    }

    public List<ProjectListResponse> getMyProjects(Long userId) {
        List<ProjectMember> myProjectMembers = projectMemberRepository.findByUserId(userId);

        if (myProjectMembers.isEmpty()) {
            return List.of();
        }

        List<Long> projectIds = myProjectMembers.stream()
                .map(ProjectMember::getProjectId)
                .toList();

        Map<Long, ProjectMemberRole> roleByProjectId = myProjectMembers.stream()
                .collect(Collectors.toMap(
                        ProjectMember::getProjectId,
                        ProjectMember::getRole
                ));

        return projectRepository.findByIdInAndStatus(projectIds, ProjectStatus.ACTIVE)
                .stream()
                .map(project -> ProjectListResponse.of(
                        project,
                        roleByProjectId.get(project.getId())
                ))
                .toList();
    }

    public ProjectDetailResponse getProjectDetail(Long userId, Long projectId) {
        Project project = getProjectOrThrow(projectId);
        ProjectMember projectMember = getProjectMemberOrThrow(projectId, userId);

        return ProjectDetailResponse.of(project, projectMember.getRole());
    }

    public List<ProjectMemberResponse> getProjectMembers(Long userId, Long projectId) {
        getProjectOrThrow(projectId);
        validateProjectMember(projectId, userId);

        return projectMemberRepository.findByProjectId(projectId)
                .stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    @Transactional
    public ProjectInviteLinkResponse createInviteLink(Long userId, Long projectId) {
        getProjectOrThrow(projectId);
        validateProjectLeader(projectId, userId);

        String inviteToken = generateUniqueInviteToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(INVITE_EXPIRE_DAYS);

        ProjectInvite projectInvite = new ProjectInvite(
                projectId,
                inviteToken,
                userId,
                expiresAt
        );

        ProjectInvite savedInvite = projectInviteRepository.save(projectInvite);

        return new ProjectInviteLinkResponse(
                savedInvite.getProjectId(),
                savedInvite.getInviteToken(),
                savedInvite.getExpiresAt()
        );
    }

    @Transactional
    public ProjectJoinResponse joinProject(Long userId, ProjectJoinRequest request) {
        ProjectInvite projectInvite = projectInviteRepository.findByInviteToken(request.inviteToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVITE_NOT_FOUND));

        if (projectInvite.isExpired()) {
            throw new BusinessException(ErrorCode.INVITE_EXPIRED);
        }

        Long projectId = projectInvite.getProjectId();

        getProjectOrThrow(projectId);

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.ALREADY_PROJECT_MEMBER);
        }

        ProjectMember projectMember = new ProjectMember(
                projectId,
                userId,
                ProjectMemberRole.MEMBER
        );

        ProjectMember savedProjectMember = projectMemberRepository.save(projectMember);

        return new ProjectJoinResponse(
                savedProjectMember.getProjectId(),
                savedProjectMember.getUserId(),
                savedProjectMember.getRole()
        );
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private ProjectMember getProjectMemberOrThrow(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PROJECT_MEMBER));
    }

    private void validateProjectMember(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    private void validateProjectLeader(Long projectId, Long userId) {
        ProjectMember projectMember = getProjectMemberOrThrow(projectId, userId);

        if (!projectMember.isLeader()) {
            throw new BusinessException(ErrorCode.PROJECT_LEADER_ONLY);
        }
    }

    private String generateUniqueInviteToken() {
        String inviteToken;

        do {
            inviteToken = UUID.randomUUID()
                    .toString()
                    .replace("-", "");
        } while (projectInviteRepository.existsByInviteToken(inviteToken));

        return inviteToken;
    }
}