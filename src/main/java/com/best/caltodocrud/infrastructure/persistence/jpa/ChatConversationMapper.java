package com.best.caltodocrud.infrastructure.persistence.jpa;

import com.best.caltodocrud.domain.ChatConversation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChatConversation Entity <-> Domain 매핑
 */
@Component
@RequiredArgsConstructor
public class ChatConversationMapper {
    
    private final ObjectMapper objectMapper;
    
    public ChatConversationEntity toEntity(ChatConversation domain) {
        try {
            return ChatConversationEntity.builder()
                .id(domain.getId())
                .sessionId(domain.getSessionId())
                .startedAt(domain.getStartedAt())
                .lastMessageAt(domain.getLastMessageAt())
                .status(domain.getStatus())
                .messages(domain.getMessages() != null ? objectMapper.writeValueAsString(domain.getMessages()) : "[]")
                .metadata(domain.getMetadata() != null ? objectMapper.writeValueAsString(domain.getMetadata()) : "{}")
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert domain to entity", e);
        }
    }
    
    public ChatConversation toDomain(ChatConversationEntity entity) {
        try {
            ChatConversation domain = ChatConversation.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .startedAt(entity.getStartedAt())
                .lastMessageAt(entity.getLastMessageAt())
                .status(entity.getStatus())
                .build();

            // JSON 문자열을 Domain 객체로 변환
            if (entity.getMessages() != null && !entity.getMessages().isEmpty()) {
                List<Map<String, Object>> messagesList = objectMapper.readValue(
                    entity.getMessages(),
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                List<ChatConversation.ChatMessage> messages = messagesList.stream()
                    .map(this::convertJsonToMessage)
                    .collect(Collectors.toList());
                domain.setMessages(messages);
            }

            // JSON 메타데이터를 Domain 객체로 변환
            if (entity.getMetadata() != null && !entity.getMetadata().isEmpty()) {
                Map<String, Object> metadataMap = objectMapper.readValue(
                    entity.getMetadata(),
                    new TypeReference<Map<String, Object>>() {}
                );
                domain.setMetadata(convertJsonToMetadata(metadataMap));
            }

            return domain;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert entity to domain", e);
        }
    }
    
    private ChatConversation.ChatMessage convertJsonToMessage(Map<String, Object> json) {
        ChatConversation.ChatMessage message = ChatConversation.ChatMessage.builder()
            .id((String) json.get("id"))
            .role((String) json.get("role"))
            .content((String) json.get("content"))
            .timestamp(LocalDateTime.parse((String) json.get("timestamp")))
            .build();
        
        if (json.containsKey("metadata")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataJson = (Map<String, Object>) json.get("metadata");
            
            ChatConversation.MessageMetadata metadata = ChatConversation.MessageMetadata.builder()
                .model((String) metadataJson.get("model"))
                .tokensUsed((Integer) metadataJson.get("tokensUsed"))
                .temperature((Double) metadataJson.get("temperature"))
                .responseTimeMs(getLongValue(metadataJson.get("responseTimeMs")))
                .summaryType((String) metadataJson.get("summaryType"))
                .build();
            
            message.setMetadata(metadata);
        }
        
        return message;
    }
    
    private ChatConversation.ConversationMetadata convertJsonToMetadata(Map<String, Object> json) {
        return ChatConversation.ConversationMetadata.builder()
            .totalMessages((Integer) json.get("totalMessages"))
            .totalTokensUsed((Integer) json.get("totalTokensUsed"))
            .totalDurationMs(getLongValue(json.get("totalDurationMs")))
            .topicsDiscussed(getListValue(json.get("topicsDiscussed")))
            .primaryIntent((String) json.get("primaryIntent"))
            .build();
    }
    
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        return Long.parseLong(value.toString());
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getListValue(Object value) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List) return (List<String>) value;
        return new ArrayList<>();
    }
}
