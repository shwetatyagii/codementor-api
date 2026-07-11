package com.codementor.service.ai;

/**
 * Contract for AI analysis providers.
 *
 * Current implementation: GeminiAiService
 * Future: OpenAiService, GroqAiService — just implement this interface.
 *
 * AnalysisService depends ONLY on this interface (Dependency Inversion Principle).
 * Switching LLM providers = zero changes in AnalysisService.
 */
public interface AiService {

    /**
     * Sends a prompt to the AI provider and returns the raw text response.
     *
     * @param prompt  fully constructed prompt string
     * @return        raw AI response text
     * @throws com.codementor.exception.AiServiceException if AI call fails
     */
    String generate(String prompt);
}