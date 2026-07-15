package com.team1.__spring_team1.domain.wireframe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.stage.service.StageService;
import com.team1.__spring_team1.domain.wireframe.dto.response.ScreenWireframeResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.entity.Wireframe;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WireframeService {

    private final WireframeRepository wireframeRepository;
    private final ScreenRepository screenRepository;
    private final StageService stageService;
    private final ProjectPermissionService projectPermissionService;
    private final ObjectMapper objectMapper;

    // 화면 목록 조회
    public List<ScreenWireframeResponse> getScreens(Long projectId, LoginUser loginUser) {
        projectPermissionService.validateProjectMember(projectId, loginUser.userId());

        StageDocument confirmedScreenSpec = stageService.getConfirmedScreenSpec(projectId);

        List<Screen> screens = screenRepository.findByStageDocumentIdOrderByScreenOrder(confirmedScreenSpec.getId());

        Map<Long, Wireframe> wireframeMap = wireframeRepository.findAllByProjectId(projectId)
                .stream()
                .collect(Collectors.toMap(Wireframe::getScreenId, Function.identity(),
                        (existing, replacement) -> existing));

        return screens.stream()
                .map(screen -> toScreenWireframeResponse
                        (screen, wireframeMap.get(screen.getId()))).toList();
    }

    // 조회
    public WireframeDslResponse getWireframe(Long screenId, LoginUser loginUser) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));

        projectPermissionService.validateProjectMember(screen.getProjectId(), loginUser.userId());

        Wireframe wireframe = wireframeRepository
                .findByScreenId(screenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WIREFRAME_NOT_FOUND));

        return deserializeWireframe(wireframe.getJsonDsl());
    }

    private ScreenWireframeResponse toScreenWireframeResponse(Screen screen, Wireframe wireframe) {
        WireframeDslResponse wireframeDsl = null;

        if (wireframe != null) {
            wireframeDsl = deserializeWireframe(wireframe.getJsonDsl());
        }
        return new ScreenWireframeResponse(screen.getId(), screen.getName(), wireframeDsl);
    }

    private WireframeDslResponse deserializeWireframe(String jsonDsl) {
        try {
            return objectMapper.readValue(jsonDsl, WireframeDslResponse.class);
        } catch (JsonProcessingException e) {
            log.error("[WireframeService] 와이어프레임 JSON 파싱 실패. jsonDsl={}, error={}", jsonDsl, e.getMessage());

            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
    }
}