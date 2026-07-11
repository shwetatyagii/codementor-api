package com.codementor.service;

import org.springframework.stereotype.Component;

/**
 * Builds structured prompts for AI analysis.
 *
 * Key design decision:
 * We ask Gemini to respond in strict JSON format.
 * This makes parsing deterministic and reliable.
 *
 * Low temperature (set in GeminiAiService) + JSON instruction
 * = consistent, parseable output every time.
 */
@Component
public class PromptBuilder {

    public String buildJavaAnalysisPrompt(String javaCode) {
        return """
            You are an expert Java code reviewer and senior software engineer.
            Analyze the following Java code thoroughly.
            
            IMPORTANT: Respond ONLY with a valid JSON object. No markdown, no backticks, no explanation outside the JSON. Keep each field concise (2-3 sentences max).
            
            Use exactly this JSON structure:
            {
              "codeQualityReview": "Overall quality assessment in 2 sentences",
              "timeComplexity": "Big O notation with brief explanation",
              "spaceComplexity": "Big O notation with brief explanation",
              "optimizationTips": "Top 2 optimization suggestions",
              "bestPractices": "Key best practices in this code",
              "alternativeApproach": "One alternative approach briefly",
              "commonMistakes": "Top 2 common mistakes to avoid",
              "interviewQuestions": [
                "Interview question 1?",
                "Interview question 2?",
                "Interview question 3?"
              ]
            }
            
            Java Code to analyze:
```java
            %s
```
            
            Remember: Return ONLY the JSON object. Nothing else. Keep responses concise.
            """.formatted(javaCode);
    }
}