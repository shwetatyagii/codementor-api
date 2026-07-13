package com.codementor.service;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildJavaAnalysisPrompt(String javaCode) {
        return """
                You are an expert Java code reviewer, algorithm specialist, and senior software engineer.
                
                Analyze the following Java code DEEPLY and SPECIFICALLY. Do NOT give generic advice.
                Every point must be SPECIFIC to THIS code — mention actual variable names, line logic, and exact issues.
                
                IMPORTANT: Respond ONLY with a valid JSON object. No markdown, no backticks, nothing outside the JSON.
                Keep each field specific and actionable. Mention actual code details.
                
                Use exactly this JSON structure:
                {
                  "codeQualityReview": "Specific review of THIS code. Mention actual variable names, method names, and concrete issues found.",
                  "timeComplexity": "Big O with explanation specific to this code's loops/operations. E.g. O(n) - single for loop at line X",
                  "spaceComplexity": "Big O with explanation. E.g. O(n) - HashMap stores up to n entries",
                  "optimizationTips": "Specific optimization for THIS code. Mention exact lines or logic that can be improved.",
                  "bestPractices": "Which best practices this code follows or violates. Be specific to the code.",
                  "alternativeApproach": "A concrete alternative with brief code hint. E.g. Instead of nested loop, use HashMap for O(1) lookup.",
                  "commonMistakes": "Mistakes present in THIS code or common pitfalls for this type of problem.",
                  "interviewQuestions": [
                    "Specific question about THIS code's approach?",
                    "Question about the data structure used in this solution?",
                    "Question about optimizing THIS specific solution?"
                  ]
                }
                
                Java Code to analyze:
```java
                %s
```
                
                Return ONLY the JSON. Be specific, not generic. Reference actual code elements.
                """.formatted(javaCode);
    }
}