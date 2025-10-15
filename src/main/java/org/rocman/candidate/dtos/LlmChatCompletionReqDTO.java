package org.rocman.candidate.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class LlmChatCompletionReqDTO {
    private String model;
    private List<ChatMessage> messages;
    private double temperature = 0.0;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage(String content) {
            this.role = "user";
            this.content = content;
        }
    }
}
