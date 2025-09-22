package com.best.caltodocrud.api.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class TodoCreateRequest {
    @NotBlank
    private String text;
    @NotBlank
    private String date; // yyyy-MM-dd
}
