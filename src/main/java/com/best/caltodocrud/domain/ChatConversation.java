package com.best.caltodocrud.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 챗봇 대화 세션 도메인 엔티티
 * PostgreSQL의 JSON 타입으로 저장될 대화 내역 관리
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {
    
    private UUID id;
    private String sessionId;  // 사용자 세션 식별자
    private LocalDateTime startedAt;
    private LocalDateTime lastMessageAt;
    private ConversationStatus status;
    
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();
    
    @Builder.Default
    private ConversationMetadata metadata = new ConversationMetadata();
    
    // 대화 상태
    public enum ConversationStatus {
        ACTIVE,
        COMPLETED,
        ARCHIVED
    }
    
    // 개별 메시지
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String id;
        private String role;  // "user" or "assistant"
        private String content;
        private LocalDateTime timestamp;
        private MessageMetadata metadata;
    }
    
    // 메시지 메타데이터
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageMetadata {
        private String model;
        private Integer tokensUsed;
        private Double temperature;
        private Long responseTimeMs;
        private String summaryType;  // "daily", "weekly", "monthly"
    }
    
    // 대화 메타데이터
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationMetadata {
        private Integer totalMessages;
        private Integer totalTokensUsed;
        private Long totalDurationMs;
        private List<String> topicsDiscussed;
        private String primaryIntent;
    }
    
    // 비즈니스 메서드
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        this.lastMessageAt = LocalDateTime.now();
        this.metadata.setTotalMessages(this.messages.size());
    }
    
    public void complete() {
        this.status = ConversationStatus.COMPLETED;
    }
    
    public void archive() {
        this.status = ConversationStatus.ARCHIVED;
    }
}
