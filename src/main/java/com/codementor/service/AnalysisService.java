package com.codementor.service;

import com.codementor.dto.request.CodeSubmissionRequest;
import com.codementor.dto.response.AnalysisResponse;
import com.codementor.entity.AnalysisResult;
import com.codementor.entity.Submission;
import com.codementor.entity.User;
import com.codementor.exception.AiServiceException;
import com.codementor.exception.ResourceNotFoundException;
import com.codementor.repository.AnalysisResultRepository;
import com.codementor.repository.SubmissionRepository;
import com.codementor.repository.UserRepository;
import com.codementor.service.ai.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AiService aiService;
    private final PromptBuilder promptBuilder;
    private final SubmissionRepository submissionRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisResponse analyzeCode(CodeSubmissionRequest request, Long userId) {

        log.info("Starting code analysis for userId: {}", userId);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Step 1: Save submission with PENDING status
        Submission submission = Submission.builder()
                .user(user)
                .codeSnippet(request.getCode())
                .title(request.getTitle() != null
                        ? request.getTitle()
                        : "Submission #" + System.currentTimeMillis())
                .status(Submission.SubmissionStatus.PENDING)
                .build();

        submission = submissionRepository.save(submission);
        log.info("Submission saved with id: {}, status: PENDING", submission.getId());

        try {
            // Step 2: Build prompt
            String prompt = promptBuilder.buildJavaAnalysisPrompt(request.getCode());

            // Step 3: Call AI (interface — works with any LLM)
            String rawAiResponse = aiService.generate(prompt);
            log.info("AI response received for submissionId: {}", submission.getId());

            // Step 4: Parse JSON response
            Map<String, Object> parsed = parseAiResponse(rawAiResponse);

            // Step 5: Build and save AnalysisResult
            AnalysisResult result = AnalysisResult.builder()
                    .submission(submission)
                    .codeQualityReview(getString(parsed, "codeQualityReview"))
                    .timeComplexity(getString(parsed, "timeComplexity"))
                    .spaceComplexity(getString(parsed, "spaceComplexity"))
                    .optimizationTips(getString(parsed, "optimizationTips"))
                    .bestPractices(getString(parsed, "bestPractices"))
                    .alternativeApproach(getString(parsed, "alternativeApproach"))
                    .commonMistakes(getString(parsed, "commonMistakes"))
                    .interviewQuestions(
                            objectMapper.writeValueAsString(
                                    parsed.get("interviewQuestions")
                            )
                    )
                    .rawAiResponse(rawAiResponse)
                    .build();

            analysisResultRepository.save(result);

            // Step 6: Update submission status to COMPLETED
            submission.setStatus(Submission.SubmissionStatus.COMPLETED);
            submissionRepository.save(submission);
            log.info("Analysis completed for submissionId: {}", submission.getId());

            // Step 7: Build and return response DTO
            return buildAnalysisResponse(submission, result, parsed);

        } catch (AiServiceException e) {
            // AI failed — mark submission as FAILED, re-throw for handler
            submission.setStatus(Submission.SubmissionStatus.FAILED);
            submissionRepository.save(submission);
            log.error("AI analysis failed for submissionId: {}. Reason: {}",
                    submission.getId(), e.getMessage());
            throw e;

        } catch (Exception e) {
            submission.setStatus(Submission.SubmissionStatus.FAILED);
            submissionRepository.save(submission);
            log.error("Unexpected error during analysis for submissionId: {}",
                    submission.getId(), e);
            throw new AiServiceException(
                    "Analysis failed due to an unexpected error. Please try again."
            );
        }
    }

    public AnalysisResponse getAnalysisById(Long submissionId, Long userId) {
        Submission submission = submissionRepository
                .findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission not found with id: " + submissionId
                ));

        AnalysisResult result = analysisResultRepository
                .findBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Analysis result not found for submission: " + submissionId
                ));

        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    result.getRawAiResponse(),
                    new TypeReference<>() {}
            );
            return buildAnalysisResponse(submission, result, parsed);
        } catch (Exception e) {
            // Fallback — build from stored fields directly
            return buildAnalysisResponseFromResult(submission, result);
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Map<String, Object> parseAiResponse(String rawResponse) {
        try {
            // Clean response — remove markdown code fences if Gemini adds them
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            return objectMapper.readValue(cleaned, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Failed to parse AI JSON response: {}", rawResponse);
            throw new AiServiceException(
                    "AI returned an unexpected format. Please try again."
            );
        }
    }

    @SuppressWarnings("unchecked")
    private AnalysisResponse buildAnalysisResponse(
            Submission submission,
            AnalysisResult result,
            Map<String, Object> parsed) {

        List<String> questions = List.of();
        try {
            Object iq = parsed.get("interviewQuestions");
            if (iq instanceof List) {
                questions = (List<String>) iq;
            }
        } catch (Exception ignored) {}

        return AnalysisResponse.builder()
                .submissionId(submission.getId())
                .title(submission.getTitle())
                .language(submission.getLanguage())
                .status(submission.getStatus().name())
                .codeQualityReview(result.getCodeQualityReview())
                .timeComplexity(result.getTimeComplexity())
                .spaceComplexity(result.getSpaceComplexity())
                .optimizationTips(result.getOptimizationTips())
                .bestPractices(result.getBestPractices())
                .alternativeApproach(result.getAlternativeApproach())
                .commonMistakes(result.getCommonMistakes())
                .interviewQuestions(questions)
                .analyzedAt(result.getCreatedAt())
                .build();
    }

    private AnalysisResponse buildAnalysisResponseFromResult(
            Submission submission,
            AnalysisResult result) {

        List<String> questions = List.of();
        try {
            questions = objectMapper.readValue(
                    result.getInterviewQuestions(),
                    new TypeReference<>() {}
            );
        } catch (Exception ignored) {}

        return AnalysisResponse.builder()
                .submissionId(submission.getId())
                .title(submission.getTitle())
                .language(submission.getLanguage())
                .status(submission.getStatus().name())
                .codeQualityReview(result.getCodeQualityReview())
                .timeComplexity(result.getTimeComplexity())
                .spaceComplexity(result.getSpaceComplexity())
                .optimizationTips(result.getOptimizationTips())
                .bestPractices(result.getBestPractices())
                .alternativeApproach(result.getAlternativeApproach())
                .commonMistakes(result.getCommonMistakes())
                .interviewQuestions(questions)
                .analyzedAt(result.getCreatedAt())
                .build();
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}