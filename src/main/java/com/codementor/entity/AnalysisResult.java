package com.codementor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one with Submission
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(name = "code_quality_review", columnDefinition = "TEXT")
    private String codeQualityReview;

    @Column(name = "time_complexity", columnDefinition = "TEXT")
    private String timeComplexity;

    @Column(name = "space_complexity", columnDefinition = "TEXT")
    private String spaceComplexity;

    @Column(name = "optimization_tips", columnDefinition = "TEXT")
    private String optimizationTips;

    @Column(name = "best_practices", columnDefinition = "TEXT")
    private String bestPractices;

    @Column(name = "alternative_approach", columnDefinition = "TEXT")
    private String alternativeApproach;

    @Column(name = "common_mistakes", columnDefinition = "TEXT")
    private String commonMistakes;

    // Stored as JSON array string: ["Q1","Q2","Q3"]
    @Column(name = "interview_questions", columnDefinition = "TEXT")
    private String interviewQuestions;

    // Full raw response from Gemini — useful for debugging
    @Column(name = "raw_ai_response", columnDefinition = "TEXT")
    private String rawAiResponse;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}