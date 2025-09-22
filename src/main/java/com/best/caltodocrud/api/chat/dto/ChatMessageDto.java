package com.best.caltodocrud.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String role; // "user" or "assistant"
    private String content;
    private Long timestamp;
}
