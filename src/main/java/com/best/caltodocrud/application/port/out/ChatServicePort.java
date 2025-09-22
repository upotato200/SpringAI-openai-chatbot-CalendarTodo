package com.best.caltodocrud.application.port.out;

import com.best.caltodocrud.api.chat.dto.ChatMessageDto;
import com.best.caltodocrud.domain.Todo;

import java.util.List;

public interface ChatServicePort {
    String chat(String message, List<ChatMessageDto> history, List<Todo> contextTodos);
}
