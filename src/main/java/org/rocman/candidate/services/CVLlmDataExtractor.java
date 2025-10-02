package org.rocman.candidate.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.rocman.candidate.dtos.CandidateProfileDTO;
import org.rocman.candidate.dtos.LlmChatCompletionReqDTO;
import org.rocman.candidate.dtos.LlmChatCompletionRespDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

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

        String prompt = """
                You are an information extraction assistant.
                TASK:
                - Extract candidate's data from the given CV text.
                - If a field is missing or cannot be identified, set its value to "N/A".
                - Keep the extracted data in the same language as the CV text.
                - Return strictly in this JSON format, with no explanations or text outside the JSON:
                
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

        LlmChatCompletionReqDTO request = new LlmChatCompletionReqDTO();
        request.setModel(model);
        request.setMessages(Collections.singletonList(new LlmChatCompletionReqDTO.ChatMessage(prompt)));
        request.setTemperature(0.0);

        try {
            log.debug("Request payload to LLM: {}", objectMapper.writeValueAsString(request));

            LlmChatCompletionRespDTO response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(LlmChatCompletionRespDTO.class)
                    .block();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("LLM returned null or empty response");
                throw new RuntimeException("Empty response from LLM");
            }

            String content = response.getChoices().get(0).getMessage().getContent();
            log.debug("Raw content from LLM: {}", content);

            CandidateProfileDTO dto = objectMapper.readValue(content, CandidateProfileDTO.class);
            log.info("Extraction completed successfully for candidate: {} {}", dto.getFirstName(), dto.getLastName());

            return dto;

        } catch (Exception e) {
            log.error("Error extracting candidate profile via LLM: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract candidate profile from LLM", e);
        }
    }
}