package com.codementor.controller;

import com.codementor.dto.response.ApiResponse;
import com.codementor.dto.response.SubmissionDetailResponse;
import com.codementor.dto.response.SubmissionHistoryResponse;
import com.codementor.security.JwtUtil;
import com.codementor.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Submission history endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Get all submissions",
            description = "Returns all submissions for the authenticated user")
    public ResponseEntity<ApiResponse<List<SubmissionHistoryResponse>>> getAllSubmissions(
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        List<SubmissionHistoryResponse> submissions =
                submissionService.getAllSubmissions(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Submissions fetched successfully", submissions)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get submission by ID",
            description = "Returns a single submission with full analysis")
    public ResponseEntity<ApiResponse<SubmissionDetailResponse>> getSubmissionById(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        SubmissionDetailResponse response =
                submissionService.getSubmissionById(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Submission fetched successfully", response)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete submission",
            description = "Deletes a submission and its analysis result")
    public ResponseEntity<ApiResponse<Void>> deleteSubmission(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        submissionService.deleteSubmission(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Submission deleted successfully")
        );
    }

    @GetMapping("/search")
    @Operation(summary = "Search submissions by title",
            description = "Searches submissions by title keyword")
    public ResponseEntity<ApiResponse<List<SubmissionHistoryResponse>>> searchSubmissions(
            @RequestParam String q,
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        List<SubmissionHistoryResponse> results =
                submissionService.searchSubmissions(q, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Search completed", results)
        );
    }

    private Long extractUserId(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(bearerToken.substring(7));
        }
        throw new IllegalStateException("No JWT token found");
    }
}