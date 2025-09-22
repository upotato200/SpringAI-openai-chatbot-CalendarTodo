package com.best.caltodocrud.api.summary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SummaryRequestDto {
    @NotBlank private String from;
    @NotBlank private String to;
    private List<TodoItemDto> todos;

    @Data
    public static class TodoItemDto {
        @NotBlank private String id;   // 프론트/도메인 문자열 id
        @NotBlank private String text;
        private Boolean done;
        @NotBlank private String date;
    }
}
