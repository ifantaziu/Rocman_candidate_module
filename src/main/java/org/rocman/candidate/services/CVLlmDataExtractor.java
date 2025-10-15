package org.rocman.candidate.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.HttpHeaders;
import org.rocman.candidate.dtos.CandidateProfileDTO;
import org.rocman.candidate.dtos.LlmChatCompletionReqDTO;
import org.rocman.candidate.dtos.LlmChatCompletionRespDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CVLlmDataExtractor {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public CVLlmDataExtractor(WebClient openaiWebClient,
                              ObjectMapper objectMapper,
                              @Value("${openai.api.model}") String model) {
        this.webClient = openaiWebClient;
        this.objectMapper = objectMapper;
        this.model = model;

        log.info("CVLlmDataExtractor initialized with model={}", model);
    }

    public CandidateProfileDTO extractCandidateProfile(String rawText) {
        log.info("Sending CV text to LLM | textLength={} chars", rawText.length());

        String safeText = rawText
                .replaceAll("\\p{C}", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String prompt = """
                You are an information extraction assistant.
                
                TASK:
                - Extract candidate's data from the given CV text.
                - If a field is missing or cannot be identified, set its value to "N/A".
                - Keep the extracted data in the same language as the CV text.
                - For the 'skill' field, extract only concrete technical skills, soft skills, tools, programming languages, applications, techniques, frameworks, or certifications.
                
                Return strictly in this JSON format, with no explanations or text outside the JSON:
                
                {
                  "email": "",
                  "phoneNumber": "",
                  "firstName": "",
                  "lastName": "",
                  "address": "",
                  "education": [{"level": "", "institution": "", "period": ""}],
                  "experience": [{"title": "", "company": "", "period": ""}],
                  "skill": [{"name": ""}],
                  "language": [{"language": "", "level": ""}]
                }
                
                CV text:
                """ + safeText;

        LlmChatCompletionReqDTO.ChatMessage chatMessage =
                new LlmChatCompletionReqDTO.ChatMessage("user", prompt);

        LlmChatCompletionReqDTO request = new LlmChatCompletionReqDTO();
        request.setModel("llama-3.1-8b-instant");
        request.setMessages(List.of(chatMessage));
        request.setTemperature(0.0);

        try {
            String jsonPayload = objectMapper.writeValueAsString(request);
            log.debug("JSON request to LLM (truncated 500 chars): {}",
                    jsonPayload.length() > 500 ? jsonPayload.substring(0, 500) + "..." : jsonPayload);

            String rawResponse = webClient.post()
                    .uri("/openai/v1/chat/completions")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(jsonPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Raw LLM response (truncated 1000 chars): {}",
                    rawResponse != null && rawResponse.length() > 1000
                            ? rawResponse.substring(0, 1000) + "..."
                            : rawResponse);

            if (rawResponse == null) {
                log.error("LLM returned null response");
                throw new RuntimeException("Empty response from LLM");
            }

            LlmChatCompletionRespDTO response = objectMapper.readValue(rawResponse, LlmChatCompletionRespDTO.class);

            if (response.getChoices() == null || response.getChoices().isEmpty() ||
                    response.getChoices().get(0).getMessage() == null) {
                log.error("LLM response structure invalid");
                throw new RuntimeException("Invalid response structure from LLM");
            }

            String content = response.getChoices().get(0).getMessage().getContent();
            log.debug("Extracted content from LLM: {}", content);

            CandidateProfileDTO dto = objectMapper.readValue(content, CandidateProfileDTO.class);
            log.info("Extraction completed successfully for candidate: {} {}", dto.getFirstName(), dto.getLastName());

            return dto;

        } catch (Exception e) {
            log.error("Error extracting candidate profile via LLM: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract candidate profile from LLM", e);
        }
    }
}