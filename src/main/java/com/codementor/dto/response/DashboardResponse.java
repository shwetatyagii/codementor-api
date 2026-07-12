package com.codementor.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private String welcomeMessage;
    private long totalSubmissions;
    private LocalDateTime lastAnalyzedAt;
    private List<SubmissionHistoryResponse> recentSubmissions;
}