package org.rocman.candidate.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.rocman.candidate.dtos.CandidateProfileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class CVLlmDataExtractor {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;


    public CVLlmDataExtractor(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.base-url}") String baseUrl,
            @Value("${openai.api.model}") String model
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
        this.model = model;

        log.info("CVLlmDataExtractor initialized with model={} and baseUrl={}", model, baseUrl);
    }

    public CandidateProfileDTO extractCandidateProfile(String rawText) {
        log.info("Sending CV text to LLM for extraction. Text length: {} characters", rawText.length());

        String prompt = """
                You are an information extraction assistant.
                TASK:
                - Extract candidate's data from the given CV text.
                - If a field is missing or cannot be identified, set its value to "N/A".
                - Keep the extracted data in the same language as the CV text.
                - Return strictly in this JSON format:
                
                {
                  "email": "",
                  "phone": "",
                  "firstName": "",
                  "lastName": "",
                  "address": "",
                  "education": [{"level": "", "institution": "", "period": ""}],
                  "experience": [{"title": "", "company": "", "period": ""}],
                  "skills": [{"name": ""}],
                  "languages": [{"language": "", "level": ""}]
                }
                
                CV text:
                """ + rawText;

        String requestBody = """
                {
                  "model": "%s",
                  "messages": [{"role":"user", "content": "%s"}],
                  "temperature": 0.0
                }
                """.formatted(model, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Raw response from LLM: {}", response);

            String content = objectMapper.readTree(response)
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            log.debug("Extracted content JSON: {}", content);

            CandidateProfileDTO dto = objectMapper.readValue(content, CandidateProfileDTO.class);
            log.info("Extraction completed successfully for candidate: {} {}", dto.getFirstName(), dto.getLastName());

            return dto;

        } catch (Exception e) {
            log.error("Error extracting candidate profile via LLM: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract candidate profile from LLM", e);
        }
    }
}
