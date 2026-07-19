package com.team1.__spring_team1.domain.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team1.__spring_team1.domain.project.dto.response.ProjectCreateResponse;
import com.team1.__spring_team1.domain.project.dto.response.ProjectJoinResponse;
import com.team1.__spring_team1.domain.project.entity.ProjectMemberRole;
import com.team1.__spring_team1.domain.project.entity.ProjectStatus;
import com.team1.__spring_team1.domain.project.service.ProjectService;
import com.team1.__spring_team1.global.exception.GlobalExceptionHandler;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerValidationTest {

    private MockMvc mockMvc;
    private ProjectService projectService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        projectService = Mockito.mock(ProjectService.class);

        ProjectController projectController = new ProjectController(projectService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new TestCurrentUserArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("프로젝트 생성 시 title이 빈 문자열이면 INVALID_INPUT 응답을 반환한다")
    void createProject_fails_whenTitleIsBlank() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "title", "",
                "description", "회의록 기반 기획 서비스",
                "goal", "MVP 기획 정리",
                "startDate", "2026-07-03",
                "endDate", "2026-07-20"
        );

        // when & then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.data.message").value("프로젝트 제목은 필수입니다."))
                .andExpect(jsonPath("$.message").doesNotExist());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 생성 시 title이 공백 문자열이면 INVALID_INPUT 응답을 반환한다")
    void createProject_fails_whenTitleIsWhitespace() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "title", "   ",
                "description", "회의록 기반 기획 서비스",
                "goal", "MVP 기획 정리",
                "startDate", "2026-07-03",
                "endDate", "2026-07-20"
        );

        // when & then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.data.message").value("프로젝트 제목은 필수입니다."));

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 생성 요청이 유효하면 ProjectService를 호출한다")
    void createProject_success_whenRequestIsValid() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "title", "AI 기획 협업 도구",
                "description", "회의록 기반 기획 서비스",
                "goal", "MVP 기획 정리",
                "startDate", "2026-07-03",
                "endDate", "2026-07-20"
        );

        ProjectCreateResponse response = new ProjectCreateResponse(
                1L,
                "AI 기획 협업 도구",
                ProjectMemberRole.LEADER,
                ProjectStatus.ACTIVE
        );

        when(projectService.createProject(eq(1L), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.title").value("AI 기획 협업 도구"))
                .andExpect(jsonPath("$.data.role").value("LEADER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(projectService).createProject(eq(1L), any());
    }

    @Test
    @DisplayName("초대 참여 시 inviteToken이 빈 문자열이면 INVALID_INPUT 응답을 반환한다")
    void joinProject_fails_whenInviteTokenIsBlank() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "inviteToken", ""
        );

        // when & then
        mockMvc.perform(post("/projects/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.data.message").value("초대 토큰은 필수입니다."));

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("초대 참여 시 inviteToken이 공백 문자열이면 INVALID_INPUT 응답을 반환한다")
    void joinProject_fails_whenInviteTokenIsWhitespace() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "inviteToken", "   "
        );

        // when & then
        mockMvc.perform(post("/projects/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.data.message").value("초대 토큰은 필수입니다."));

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("초대 참여 요청이 유효하면 ProjectService를 호출한다")
    void joinProject_success_whenRequestIsValid() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "inviteToken", "valid-token"
        );

        ProjectJoinResponse response = new ProjectJoinResponse(
                1L,
                "AI 기획 협업 도구",
                ProjectMemberRole.MEMBER
        );

        when(projectService.joinProject(eq(1L), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/projects/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.title").value("AI 기획 협업 도구"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));

        verify(projectService).joinProject(eq(1L), any());
    }

    private static class TestCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class)
                    && parameter.getParameterType().equals(LoginUser.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return new LoginUser(1L, "mock-user-1", "개발용 사용자 1");
        }
    }
}