package com.best.caltodocrud.api.chat;

import com.best.caltodocrud.api.chat.dto.ChatMessageDto;
import com.best.caltodocrud.api.chat.dto.ChatRequestDto;
import com.best.caltodocrud.api.chat.dto.ChatResponseDto;
import com.best.caltodocrud.application.port.in.chat.ChatUseCase;
import com.best.caltodocrud.application.port.in.todo.GetTodosUseCase;
import com.best.caltodocrud.domain.Todo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final GetTodosUseCase getTodosUseCase;

    @GetMapping("/message")
    public ChatResponseDto chatStatus() {
        return ChatResponseDto.builder()
                .message("Chat service is running. Use POST method to send messages.")
                .timestamp(System.currentTimeMillis())
                .success(true)
                .build();
    }

    @PostMapping("/message")
    public ChatResponseDto chat(@RequestBody @Valid ChatRequestDto request) {
        log.info("Chat request: {}", request.getMessage());
        
        try {
            // 현재 날짜의 할 일 목록을 컨텍스트로 제공
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<Todo> todayTodos = getTodosUseCase.findByDate(today);
            
            String response = chatUseCase.chat(request.getMessage(), request.getHistory(), todayTodos);
            
            return ChatResponseDto.builder()
                    .message(response)
                    .timestamp(System.currentTimeMillis())
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("Chat failed: {}", e.getMessage(), e);

            String errorMessage;
            if (e.getMessage() != null) {
                if (e.getMessage().contains("할당량이 초과")) {
                    errorMessage = "OpenAI API 할당량이 초과되었습니다. 잠시 후 다시 시도해주세요.";
                } else if (e.getMessage().contains("요청 한도 초과")) {
                    errorMessage = "API 요청 한도가 초과되었습니다. 잠시 후 다시 시도해주세요.";
                } else if (e.getMessage().contains("시간 초과")) {
                    errorMessage = "응답 시간이 초과되었습니다. 다시 시도해주세요.";
                } else if (e.getMessage().contains("unauthorized") || e.getMessage().contains("401")) {
                    errorMessage = "API 키 인증에 실패했습니다. 설정을 확인해주세요.";
                } else {
                    errorMessage = "죄송합니다. 일시적인 오류가 발생했습니다: " + e.getMessage();
                }
            } else {
                errorMessage = "죄송합니다. 일시적인 오류가 발생했습니다. 다시 시도해주세요.";
            }

            return ChatResponseDto.builder()
                    .message(errorMessage)
                    .timestamp(System.currentTimeMillis())
                    .success(false)
                    .build();
        }
    }
}
