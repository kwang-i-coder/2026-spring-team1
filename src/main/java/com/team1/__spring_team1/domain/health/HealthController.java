package com.team1.__spring_team1.domain.health;

import com.team1.__spring_team1.global.exception.BusinessException;
import com.team1.__spring_team1.global.exception.ErrorCode;
import com.team1.__spring_team1.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("OK");
    }

    @GetMapping("/health/error")
    public ApiResponse<String> healthError() {
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
}
