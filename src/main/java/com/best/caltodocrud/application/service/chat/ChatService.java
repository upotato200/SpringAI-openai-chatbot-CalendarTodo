package com.best.caltodocrud.application.service.chat;

import com.best.caltodocrud.api.chat.dto.ChatMessageDto;
import com.best.caltodocrud.application.port.in.chat.ChatUseCase;
import com.best.caltodocrud.application.port.out.ChatServicePort;
import com.best.caltodocrud.application.port.out.ChatConversationRepositoryPort;
import com.best.caltodocrud.domain.ChatConversation;
import com.best.caltodocrud.domain.Todo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class ChatService implements ChatUseCase {

    private final ChatServicePort chatServicePort;
    private final ChatConversationRepositoryPort conversationRepository;

    public ChatService(ChatServicePort chatServicePort,
                      ChatConversationRepositoryPort conversationRepository) {
        this.chatServicePort = chatServicePort;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public String chat(String message, List<ChatMessageDto> history, List<Todo> contextTodos) {
        log.info("Processing chat message: {}", message);

        // AI 응답 시작 시간
        long startTime = System.currentTimeMillis();

        String response = null;
        try {
            // OpenAI API 호출 (트랜잭션 없이 실행)
            response = chatServicePort.chat(message, history, contextTodos);
            log.info("Chat response received successfully");
        } catch (Exception e) {
            log.error("Chat service failed: {}", e.getMessage(), e);
            throw e; // 예외를 그대로 던져서 정확한 오류 메시지가 전달되도록 함
        }

        // 응답 시간 계산
        long responseTime = System.currentTimeMillis() - startTime;

        // PostgreSQL에 대화 저장 - 별도 트랜잭션에서 처리
        try {
            log.info("Attempting to save conversation to database...");
            saveConversationInNewTransaction(message, response, history, responseTime, contextTodos);
            log.info("Conversation saved successfully to database");
        } catch (Exception e) {
            // 저장 실패해도 응답은 정상 반환
            log.error("Failed to save conversation to database: {}", e.getMessage(), e);
        }

        log.info("Chat response generated in {}ms", responseTime);

        return response;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
    private void saveConversationInNewTransaction(String message, String response,
                                                 List<ChatMessageDto> history,
                                                 long responseTime,
                                                 List<Todo> contextTodos) {
        saveConversation(message, response, history, responseTime, contextTodos);
    }

    private void saveConversation(String message, String response,
                                  List<ChatMessageDto> history,
                                  long responseTime,
                                  List<Todo> contextTodos) {

        // 세션 ID 생성 (실제로는 HTTP 세션이나 JWT에서 추출)
        String sessionId = generateSessionId();

        // 기존 대화 가져오기 또는 새 대화 시작
        ChatConversation conversation = conversationRepository
                .findActiveBySessionId(sessionId)
                .orElseGet(() -> createNewConversation(sessionId));

        // 사용자 메시지 추가
        ChatConversation.ChatMessage userMessage = ChatConversation.ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .role("user")
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();

        // AI 응답 메시지 생성
        ChatConversation.ChatMessage assistantMessage = ChatConversation.ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .role("assistant")
                .content(response)
                .timestamp(LocalDateTime.now())
                .metadata(ChatConversation.MessageMetadata.builder()
                        .model("gpt-4o-mini")
                        .tokensUsed(estimateTokens(message + response))
                        .temperature(0.2)
                        .responseTimeMs(responseTime)
                        .summaryType(detectSummaryType(message))
                        .build())
                .build();

        // 메시지들을 대화에 추가
        List<ChatConversation.ChatMessage> currentMessages = conversation.getMessages();
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
            conversation.setMessages(currentMessages);
        }
        currentMessages.add(userMessage);
        currentMessages.add(assistantMessage);

        // 마지막 메시지 시간 업데이트
        conversation.setLastMessageAt(LocalDateTime.now());

        // 메타데이터 업데이트
        updateConversationMetadata(conversation, message, responseTime);

        // PostgreSQL에 JSONB로 저장
        conversationRepository.save(conversation);

        log.info("Conversation saved to PostgreSQL with {} messages",
                conversation.getMessages().size());
    }

    private ChatConversation createNewConversation(String sessionId) {
        return ChatConversation.builder()
                .id(UUID.randomUUID())
                .sessionId(sessionId)
                .startedAt(LocalDateTime.now())
                .lastMessageAt(LocalDateTime.now())
                .status(ChatConversation.ConversationStatus.ACTIVE)
                .messages(new ArrayList<>())
                .metadata(ChatConversation.ConversationMetadata.builder()
                        .totalMessages(0)
                        .totalTokensUsed(0)
                        .totalDurationMs(0L)
                        .topicsDiscussed(new ArrayList<>())
                        .build())
                .build();
    }

    private void updateConversationMetadata(ChatConversation conversation,
                                            String message,
                                            long responseTime) {
        ChatConversation.ConversationMetadata metadata = conversation.getMetadata();
        if (metadata == null) {
            metadata = ChatConversation.ConversationMetadata.builder()
                    .totalMessages(0)
                    .totalTokensUsed(0)
                    .totalDurationMs(0L)
                    .topicsDiscussed(new ArrayList<>())
                    .build();
            conversation.setMetadata(metadata);
        }

        // 메시지 수 업데이트 (사용자 + AI 응답 = 2개 추가)
        int currentMessages = metadata.getTotalMessages() != null ? metadata.getTotalMessages() : 0;
        metadata.setTotalMessages(currentMessages + 2);

        // 토큰 사용량 업데이트
        int currentTokens = metadata.getTotalTokensUsed() != null ? metadata.getTotalTokensUsed() : 0;
        metadata.setTotalTokensUsed(currentTokens + estimateTokens(message));

        // 총 시간 업데이트
        long totalDuration = metadata.getTotalDurationMs() != null ? metadata.getTotalDurationMs() : 0L;
        metadata.setTotalDurationMs(totalDuration + responseTime);

        // 주제 추출 및 업데이트
        List<String> topics = extractTopics(message);
        List<String> existingTopics = metadata.getTopicsDiscussed() != null ?
                metadata.getTopicsDiscussed() : new ArrayList<>();

        topics.forEach(topic -> {
            if (!existingTopics.contains(topic)) {
                existingTopics.add(topic);
            }
        });
        metadata.setTopicsDiscussed(existingTopics);

        // 주요 의도 파악
        if (metadata.getPrimaryIntent() == null) {
            metadata.setPrimaryIntent(detectIntent(message));
        }
    }

    private String generateSessionId() {
        // 실제로는 HTTP Session ID나 JWT token에서 추출
        // 여기서는 임시로 날짜 기반 세션 ID 생성
        return "session-" + LocalDateTime.now().toLocalDate().toString();
    }

    private int estimateTokens(String text) {
        // 간단한 토큰 추정 (실제로는 tiktoken 라이브러리 사용)
        // 평균적으로 한글은 2-3자당 1토큰, 영어는 4자당 1토큰
        return text.length() / 3;
    }

    private String detectSummaryType(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("오늘") || lowerMessage.contains("today")) {
            return "daily";
        } else if (lowerMessage.contains("이번 주") || lowerMessage.contains("week")) {
            return "weekly";
        } else if (lowerMessage.contains("이번 달") || lowerMessage.contains("month")) {
            return "monthly";
        } else if (lowerMessage.contains("내일") || lowerMessage.contains("tomorrow")) {
            return "tomorrow";
        }

        return "general";
    }

    private List<String> extractTopics(String message) {
        List<String> topics = new ArrayList<>();
        String lowerMessage = message.toLowerCase();

        // 주제 키워드 매핑
        Map<String, String> topicKeywords = new HashMap<>();
        topicKeywords.put("일정", "schedule");
        topicKeywords.put("할 일", "todo");
        topicKeywords.put("요약", "summary");
        topicKeywords.put("통계", "statistics");
        topicKeywords.put("완료", "completed");
        topicKeywords.put("미완료", "pending");
        topicKeywords.put("오늘", "today");
        topicKeywords.put("내일", "tomorrow");
        topicKeywords.put("이번 주", "week");
        topicKeywords.put("이번 달", "month");

        topicKeywords.forEach((keyword, topic) -> {
            if (lowerMessage.contains(keyword)) {
                topics.add(topic);
            }
        });

        return topics;
    }

    private String detectIntent(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("요약") || lowerMessage.contains("정리")) {
            return "summarize";
        } else if (lowerMessage.contains("추가") || lowerMessage.contains("생성")) {
            return "create";
        } else if (lowerMessage.contains("삭제") || lowerMessage.contains("제거")) {
            return "delete";
        } else if (lowerMessage.contains("수정") || lowerMessage.contains("변경")) {
            return "update";
        } else if (lowerMessage.contains("분석") || lowerMessage.contains("통계")) {
            return "analyze";
        }

        return "query";
    }
}