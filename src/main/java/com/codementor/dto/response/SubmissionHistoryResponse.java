package com.codementor.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubmissionHistoryResponse {

    private Long id;
    private String title;
    private String language;
    private String status;
    private String timeComplexity;
    private String spaceComplexity;
    private String codeSnippetPreview;  // First 100 chars of code
    private LocalDateTime submittedAt;
}