package com.best.caltodocrud.api.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
    
    @NotBlank(message = "메시지는 필수입니다")
    private String message;
    
    private List<ChatMessageDto> history;
}
