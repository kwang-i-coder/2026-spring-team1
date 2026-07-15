package com.team1.__spring_team1.domain.wireframe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.__spring_team1.domain.ai.dto.WireframeContent;
import com.team1.__spring_team1.domain.ai.service.AiDocumentService;
import com.team1.__spring_team1.domain.project.service.ProjectPermissionService;
import com.team1.__spring_team1.domain.stage.entity.Screen;
import com.team1.__spring_team1.domain.stage.entity.StageDocument;
import com.team1.__spring_team1.domain.stage.repository.ScreenRepository;
import com.team1.__spring_team1.domain.stage.service.StageService;
import com.team1.__spring_team1.domain.wireframe.dto.request.WireframeGenerateRequest;
import com.team1.__spring_team1.domain.wireframe.dto.response.ScreenWireframeResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeDslResponse;
import com.team1.__spring_team1.domain.wireframe.dto.response.WireframeElementResponse;
import com.team1.__spring_team1.domain.wireframe.entity.Wireframe;
import com.team1.__spring_team1.domain.wireframe.repository.WireframeRepository;
import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.List;
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
    private final AiDocumentService aiDocumentService;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<ScreenWireframeResponse> generateWireframe(Long projectId, WireframeGenerateRequest request, LoginUser loginUser) {
        projectPermissionService.validateProjectMember(projectId, loginUser.userId());

        StageDocument confirmedScreenSpec = stageService.getConfirmedScreenSpec(projectId);

        List<Screen> latestScreens = screenRepository.findByStageDocumentIdOrderByScreenOrder(confirmedScreenSpec.getId());

        Map<Long, Screen> latestScreenMap = latestScreens.stream().collect(Collectors.toMap(Screen::getId, Function.identity()));

        List<Long> requestedScreenIds = new ArrayList<>(new LinkedHashSet<>(request.screenIds()));

        List<Screen> requestedScreens = requestedScreenIds.stream()
                .map(screenId -> {
                    Screen screen = latestScreenMap.get(screenId);

                    if (screen == null) {
                        throw new  BusinessException(ErrorCode.SCREEN_NOT_FOUND);
                    }

                    return screen;
                })
                .toList();

        Map<Long, Wireframe> existingWireframeMap = wireframeRepository.findAllByProjectId(projectId)
                .stream()
                .collect(Collectors.toMap(Wireframe::getScreenId, Function.identity(), (existing, replacement) -> existing));

        Map<Long, WireframeDslResponse> resultDslMap = new HashMap<>();
        List<Wireframe> newWireframes = new ArrayList<>();

        for (Screen screen : requestedScreens) {
            Wireframe existingWireframe = existingWireframeMap.get(screen.getId());

            if (existingWireframe != null) {
                WireframeDslResponse existingDsl = deserializeWireframe(existingWireframe.getJsonDsl());
                resultDslMap.put(screen.getId(), existingDsl);

                continue;
            }

            WireframeContent generatedContent = aiDocumentService.generateWireframe(screen.getSpecJson());
            WireframeDslResponse generatedDsl = toWireframeDslResponse(generatedContent);

            String jsonDsl = serializeWireframe(generatedDsl);

            Wireframe newWireframe = new Wireframe(projectId, screen.getId(), jsonDsl);

            newWireframes.add(newWireframe);
            resultDslMap.put(screen.getId(), generatedDsl);
        }

        if (!newWireframes.isEmpty()) {
            wireframeRepository.saveAll(newWireframes);
        }
        return requestedScreens.stream().map(screen -> new ScreenWireframeResponse(screen.getId(), screen.getName(), resultDslMap.get(screen.getId())))
                .toList();
    }

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

    private WireframeDslResponse toWireframeDslResponse(WireframeContent content) {
        List<WireframeElementResponse> elements = content.getElements()
                .stream()
                .map(element -> new WireframeElementResponse(
                                element.getId(),
                                element.getType(),
                                element.getText(),
                                element.getX(),
                                element.getY(),
                                element.getW(),
                                element.getH()
                        ))
                .toList();

        return new WireframeDslResponse(content.getType(), content.getWidth(), content.getHeight(), elements);
    }

    private String serializeWireframe(WireframeDslResponse wireframeDsl) {
        try {
            return objectMapper.writeValueAsString(wireframeDsl);
        } catch (JsonProcessingException e) {
            log.error(
                    "[WireframeService] 와이어프레임 JSON 직렬화 실패. error={}", e.getMessage());
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
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