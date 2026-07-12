package com.codementor.service;

import com.codementor.dto.response.SubmissionDetailResponse;
import com.codementor.dto.response.SubmissionHistoryResponse;
import com.codementor.entity.AnalysisResult;
import com.codementor.entity.Submission;
import com.codementor.exception.ResourceNotFoundException;
import com.codementor.repository.AnalysisResultRepository;
import com.codementor.repository.SubmissionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ObjectMapper objectMapper;

    // Get all submissions for a user
    public List<SubmissionHistoryResponse> getAllSubmissions(Long userId) {
        log.info("Fetching all submissions for userId: {}", userId);

        return submissionRepository
                .findByUserIdOrderBySubmittedAtDesc(userId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // Get single submission with full analysis
    public SubmissionDetailResponse getSubmissionById(Long submissionId, Long userId) {
        log.info("Fetching submission id: {} for userId: {}", submissionId, userId);

        Submission submission = submissionRepository
                .findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission not found with id: " + submissionId
                ));

        AnalysisResult result = analysisResultRepository
                .findBySubmissionId(submissionId)
                .orElse(null);

        return toDetailResponse(submission, result);
    }

    // Delete a submission
    @Transactional
    public void deleteSubmission(Long submissionId, Long userId) {
        log.info("Deleting submission id: {} for userId: {}", submissionId, userId);

        Submission submission = submissionRepository
                .findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission not found with id: " + submissionId
                ));

        submissionRepository.delete(submission);
        log.info("Submission deleted: {}", submissionId);
    }

    // Search submissions by title
    public List<SubmissionHistoryResponse> searchSubmissions(String keyword, Long userId) {
        log.info("Searching submissions for userId: {} with keyword: {}", userId, keyword);

        return submissionRepository
                .searchByTitleAndUserId(keyword, userId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private SubmissionHistoryResponse toHistoryResponse(Submission submission) {
        String timeComplexity = null;
        String spaceComplexity = null;

        // Get complexity from analysis result if available
        if (submission.getStatus() == Submission.SubmissionStatus.COMPLETED) {
            AnalysisResult result = analysisResultRepository
                    .findBySubmissionId(submission.getId())
                    .orElse(null);
            if (result != null) {
                timeComplexity = result.getTimeComplexity();
                spaceComplexity = result.getSpaceComplexity();
            }
        }

        // Code preview — first 100 chars
        String codePreview = submission.getCodeSnippet();
        if (codePreview != null && codePreview.length() > 100) {
            codePreview = codePreview.substring(0, 100) + "...";
        }

        return SubmissionHistoryResponse.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .language(submission.getLanguage())
                .status(submission.getStatus().name())
                .timeComplexity(timeComplexity)
                .spaceComplexity(spaceComplexity)
                .codeSnippetPreview(codePreview)
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    private SubmissionDetailResponse toDetailResponse(
            Submission submission,
            AnalysisResult result) {

        SubmissionDetailResponse.SubmissionDetailResponseBuilder builder =
                SubmissionDetailResponse.builder()
                        .submissionId(submission.getId())
                        .title(submission.getTitle())
                        .language(submission.getLanguage())
                        .status(submission.getStatus().name())
                        .codeSnippet(submission.getCodeSnippet())
                        .submittedAt(submission.getSubmittedAt());

        if (result != null) {
            List<String> questions = parseInterviewQuestions(
                    result.getInterviewQuestions()
            );

            builder.codeQualityReview(result.getCodeQualityReview())
                    .timeComplexity(result.getTimeComplexity())
                    .spaceComplexity(result.getSpaceComplexity())
                    .optimizationTips(result.getOptimizationTips())
                    .bestPractices(result.getBestPractices())
                    .alternativeApproach(result.getAlternativeApproach())
                    .commonMistakes(result.getCommonMistakes())
                    .interviewQuestions(questions)
                    .analyzedAt(result.getCreatedAt());
        }

        return builder.build();
    }

    private List<String> parseInterviewQuestions(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Could not parse interview questions JSON: {}", json);
            return List.of();
        }
    }
}