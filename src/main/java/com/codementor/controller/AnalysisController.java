package com.codementor.controller;

import com.codementor.dto.request.CodeSubmissionRequest;
import com.codementor.dto.response.AnalysisResponse;
import com.codementor.dto.response.ApiResponse;
import com.codementor.security.JwtUtil;
import com.codementor.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Java code analysis endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final JwtUtil jwtUtil;

    @PostMapping("/submit")
    @Operation(
            summary = "Submit Java code for AI analysis",
            description = "Submits Java code to Gemini AI for detailed analysis. " +
                    "Returns complexity, quality review, and interview questions."
    )
    public ResponseEntity<ApiResponse<AnalysisResponse>> submitCode(
            @Valid @RequestBody CodeSubmissionRequest request,
            HttpServletRequest httpRequest) {

        Long userId = extractUserIdFromRequest(httpRequest);
        AnalysisResponse response = analysisService.analyzeCode(request, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Code analyzed successfully", response)
        );
    }

    @GetMapping("/{submissionId}")
    @Operation(
            summary = "Get analysis by submission ID",
            description = "Retrieves a previously analyzed submission. " +
                    "Only the owner can access their submission."
    )
    public ResponseEntity<ApiResponse<AnalysisResponse>> getAnalysis(
            @PathVariable Long submissionId,
            HttpServletRequest httpRequest) {

        Long userId = extractUserIdFromRequest(httpRequest);
        AnalysisResponse response = analysisService.getAnalysisById(submissionId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Analysis fetched successfully", response)
        );
    }

    // Extract userId from JWT token in Authorization header
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new IllegalStateException("No JWT token found in request");
    }
}