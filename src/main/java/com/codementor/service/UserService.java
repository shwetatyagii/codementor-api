package com.codementor.service;

import com.codementor.dto.request.UpdateProfileRequest;
import com.codementor.dto.response.DashboardResponse;
import com.codementor.dto.response.SubmissionHistoryResponse;
import com.codementor.entity.Submission;
import com.codementor.entity.User;
import com.codementor.exception.ResourceNotFoundException;
import com.codementor.repository.AnalysisResultRepository;
import com.codementor.repository.SubmissionRepository;
import com.codementor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final AnalysisResultRepository analysisResultRepository;

    public DashboardResponse getDashboard(Long userId) {
        log.info("Fetching dashboard for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long totalSubmissions = submissionRepository.countByUserId(userId);

        // Recent 5 submissions
        List<Submission> recentList =
                submissionRepository.findTop5ByUserIdOrderBySubmittedAtDesc(userId);

        // Last analyzed at — from most recent COMPLETED submission
        var lastAnalyzedAt = recentList.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.COMPLETED)
                .map(Submission::getSubmittedAt)
                .findFirst()
                .orElse(null);

        List<SubmissionHistoryResponse> recentResponses = recentList.stream()
                .map(s -> {
                    String timeComplexity = null;
                    String spaceComplexity = null;

                    if (s.getStatus() == Submission.SubmissionStatus.COMPLETED) {
                        var result = analysisResultRepository
                                .findBySubmissionId(s.getId())
                                .orElse(null);
                        if (result != null) {
                            timeComplexity = result.getTimeComplexity();
                            spaceComplexity = result.getSpaceComplexity();
                        }
                    }

                    String codePreview = s.getCodeSnippet();
                    if (codePreview != null && codePreview.length() > 100) {
                        codePreview = codePreview.substring(0, 100) + "...";
                    }

                    return SubmissionHistoryResponse.builder()
                            .id(s.getId())
                            .title(s.getTitle())
                            .language(s.getLanguage())
                            .status(s.getStatus().name())
                            .timeComplexity(timeComplexity)
                            .spaceComplexity(spaceComplexity)
                            .codeSnippetPreview(codePreview)
                            .submittedAt(s.getSubmittedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .welcomeMessage("Hello, " + user.getFullName())
                .totalSubmissions(totalSubmissions)
                .lastAnalyzedAt(lastAnalyzedAt)
                .recentSubmissions(recentResponses)
                .build();
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        userRepository.save(user);
        log.info("Profile updated for userId: {}", userId);
    }
}