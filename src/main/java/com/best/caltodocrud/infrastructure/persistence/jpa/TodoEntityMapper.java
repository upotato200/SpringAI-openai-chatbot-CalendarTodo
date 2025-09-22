package com.best.caltodocrud.infrastructure.persistence.jpa;

import com.best.caltodocrud.domain.Todo;
import java.time.LocalDateTime;

final class TodoEntityMapper {
    private TodoEntityMapper(){}

    static TodoEntity toEntity(Todo t) {
        return TodoEntity.builder()
                .id(t.getId())
                .text(t.getText())
                .done(t.isDone())
                .date(t.getDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    static Todo toDomain(TodoEntity e) {
        if (e == null) return null;
        return new Todo(
                e.getId(),
                e.getText(),
                e.isDone(),  // primitive boolean 사용
                e.getDate()
        );
    }
}
