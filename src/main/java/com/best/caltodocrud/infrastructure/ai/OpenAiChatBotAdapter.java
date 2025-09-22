package com.best.caltodocrud.infrastructure.ai;

import com.best.caltodocrud.api.chat.dto.ChatMessageDto;
import com.best.caltodocrud.application.port.out.ChatServicePort;
import com.best.caltodocrud.domain.Todo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class OpenAiChatBotAdapter implements ChatServicePort {

    private final ChatClient chat;

    @Override
    public String chat(String message, List<ChatMessageDto> history, List<Todo> contextTodos) {
        try {
            log.info("Starting OpenAI chat request for message: {}", message);
            List<Message> messages = buildMessages(message, history, contextTodos);

            // 타임아웃을 빠르게 설정하여 블로킹 방지
            String response = chat
                    .prompt()
                    .messages(messages)
                    .call()
                    .content();

            log.info("AI chat response generated successfully");
            return response;

        } catch (Exception e) {
            log.error("OpenAI chat failed: {}", e.getMessage());
            log.debug("OpenAI chat error details", e);

            // 구체적인 오류 정보를 포함한 예외를 다시 던짐
            if (e.getMessage() != null && e.getMessage().contains("insufficient_quota")) {
                throw new RuntimeException("OpenAI 할당량이 초과되었습니다", e);
            } else if (e.getMessage() != null && e.getMessage().contains("429")) {
                throw new RuntimeException("OpenAI API 요청 한도 초과", e);
            } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw new RuntimeException("OpenAI API 응답 시간 초과", e);
            } else {
                throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
            }
        }
    }

    private List<Message> buildMessages(String currentMessage, List<ChatMessageDto> history, List<Todo> contextTodos) {
        List<Message> messages = new ArrayList<>();
        
        // 시스템 메시지 (컨텍스트 포함)
        String systemPrompt = buildSystemPrompt(contextTodos);
        messages.add(new SystemMessage(systemPrompt));
        
        // 대화 히스토리 추가 (최근 10개만)
        if (history != null && !history.isEmpty()) {
            List<ChatMessageDto> recentHistory = history.size() > 10 
                ? history.subList(history.size() - 10, history.size()) 
                : history;
                
            for (ChatMessageDto msg : recentHistory) {
                if ("user".equals(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }
        
        // 현재 사용자 메시지
        messages.add(new UserMessage(currentMessage));
        
        return messages;
    }

    private String buildSystemPrompt(List<Todo> contextTodos) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 간결하고 실용적인 할 일 관리 어시스턴트입니다.\n");
        prompt.append("현재 날짜: ").append(today).append("\n\n");

        if (contextTodos != null && !contextTodos.isEmpty()) {
            prompt.append("오늘의 할 일:\n");
            int totalTodos = contextTodos.size();
            long completedTodos = contextTodos.stream().mapToLong(t -> t.isDone() ? 1 : 0).sum();

            for (Todo todo : contextTodos) {
                String status = todo.isDone() ? "✅" : "◯";
                prompt.append("  ").append(status).append(" ").append(todo.getText()).append("\n");
            }

            prompt.append("진행률: ").append(completedTodos).append("/").append(totalTodos).append("\n\n");
        } else {
            prompt.append("오늘 등록된 할 일이 없습니다.\n\n");
        }

        prompt.append("응답 규칙:\n");
        prompt.append("- 3줄 이내로 간결하게 답변\n");
        prompt.append("- 필요시에만 이모지 사용\n");
        prompt.append("- 구체적이고 실행 가능한 조언 제공\n");
        prompt.append("- 불필요한 격려나 부연설명 최소화\n");

        return prompt.toString();
    }

    private String getErrorMessage(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
            return "잠시만요! AI 서비스가 많이 사용되고 있어요. 😅\n잠깐 기다렸다가 다시 말씀해주시겠어요?";
        } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
            return "응답이 조금 지연되고 있어요. ⏰\n다시 한 번 말씀해주시겠어요?";
        } else {
            return "죄송해요, 일시적인 문제가 발생했어요. 🔧\n잠시 후 다시 시도해주세요!";
        }
    }
}
