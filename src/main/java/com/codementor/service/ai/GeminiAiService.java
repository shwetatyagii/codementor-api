package com.codementor.service.ai;

import com.codementor.config.GeminiConfig;
import com.codementor.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService implements AiService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;

    @Override
    public String generate(String prompt) {
        String url = geminiConfig.getGeminiApiUrl();

        Map<String, Object> requestBody = buildRequestBody(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(geminiConfig.getGeminiApiKey());

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        log.info("Sending request to Groq API...");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return extractTextFromResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Groq API client error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new AiServiceException(
                        "AI service rate limit reached. Please try again in a moment."
                );
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AiServiceException(
                        "AI service authentication failed. Please contact support."
                );
            }
            throw new AiServiceException(
                    "AI service rejected the request: " + e.getStatusCode()
            );

        } catch (HttpServerErrorException e) {
            log.error("Groq API server error: {}", e.getStatusCode());
            throw new AiServiceException(
                    "AI service is temporarily unavailable. Please try again later."
            );

        } catch (ResourceAccessException e) {
            log.error("Groq API timeout/network error: {}", e.getMessage());
            throw new AiServiceException(
                    "AI service request timed out. Please try again."
            );
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        return Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(message),
                "temperature", 0.3,
                "max_tokens", 2048
        );
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) responseBody.get("choices");

            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");

            String text = (String) message.get("content");

            log.info("Groq response received successfully, length: {}", text.length());
            return text;

        } catch (Exception e) {
            log.error("Failed to parse Groq response: {}", responseBody);
            throw new AiServiceException(
                    "Failed to parse AI response. Please try again."
            );
        }
    }
}