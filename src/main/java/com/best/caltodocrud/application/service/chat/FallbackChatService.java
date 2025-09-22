package com.best.caltodocrud.application.service.chat;

import com.best.caltodocrud.api.chat.dto.ChatMessageDto;
import com.best.caltodocrud.application.port.in.chat.ChatUseCase;
import com.best.caltodocrud.domain.Todo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class FallbackChatService implements ChatUseCase {

    @Override
    public String chat(String message, List<ChatMessageDto> history, List<Todo> contextTodos) {
        log.warn("AI service is disabled - returning fallback response");

        return "AI 서비스가 비활성화되어 있습니다. " +
               "OpenAI API 키를 설정하고 app.ai.enabled=true로 변경해주세요.";
    }
}