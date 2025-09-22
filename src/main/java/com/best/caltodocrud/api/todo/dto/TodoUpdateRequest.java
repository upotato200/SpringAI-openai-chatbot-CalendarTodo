package com.best.caltodocrud.api.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class TodoUpdateRequest {
    @NotBlank
    private String text;
    @NotNull
    private Boolean done;
}
