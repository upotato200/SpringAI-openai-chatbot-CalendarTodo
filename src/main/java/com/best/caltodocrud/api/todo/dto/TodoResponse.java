package com.best.caltodocrud.api.todo.dto;

import com.best.caltodocrud.domain.Todo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodoResponse {
    private Long id;
    private String text;
    private Boolean done;
    private String date;

    public static TodoResponse from(Todo t) {
        if (t == null) return null;
        return TodoResponse.builder()
                .id(t.getId())
                .text(t.getText())
                .done(t.isDone())
                .date(t.getDate())
                .build();
    }
}
