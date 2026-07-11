package com.codementor.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AnalysisResponse {

    private Long submissionId;
    private String title;
    private String language;
    private String status;

    // AI Analysis fields
    private String codeQualityReview;
    private String timeComplexity;
    private String spaceComplexity;
    private String optimizationTips;
    private String bestPractices;
    private String alternativeApproach;
    private String commonMistakes;
    private List<String> interviewQuestions;

    private LocalDateTime analyzedAt;
}