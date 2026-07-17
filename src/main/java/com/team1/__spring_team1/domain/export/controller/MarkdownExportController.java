package com.team1.__spring_team1.domain.export.controller;

import com.team1.__spring_team1.domain.export.service.MarkdownExportService;
import com.team1.__spring_team1.global.security.CurrentUser;
import com.team1.__spring_team1.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MarkdownExportController {

    private final MarkdownExportService markdownExportService;

    @Operation(summary = "Markdown 리포트 export", description = "프로젝트의 확정된 산출물을 Markdown 문자열로 반환한다.")
    @GetMapping(value = "/projects/{projectId}/export/markdown", produces = "text/markdown;charset=UTF-8")
    public ResponseEntity<String> exportMarkdown(
            @PathVariable Long projectId,
            @CurrentUser LoginUser loginUser
    ) {
        String markdown = markdownExportService.exportMarkdown(projectId, loginUser);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
                .body(markdown);
    }
}