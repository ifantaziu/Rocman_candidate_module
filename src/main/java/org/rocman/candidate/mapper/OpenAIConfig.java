package org.rocman.candidate.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class OpenAIConfig {
    @Bean
    public WebClient openaiWebClient(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.base-url}") String baseUrl
    ) {
        log.info("Initializing OpenAI WebClient | baseUrl={}", baseUrl);
        log.info("API key prefix={}", apiKey.substring(0, 6));
        log.info("Initializing OpenAI WebClient | baseUrl={} ", baseUrl);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    @Bean
    public ObjectMapper objectMapper() {
        log.info("Registering global ObjectMapper with default Spring modules");
        return new ObjectMapper()
                .findAndRegisterModules();
    }
}
