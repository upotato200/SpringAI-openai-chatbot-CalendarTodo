package com.best.caltodocrud.application.port.in.todo;

import com.best.caltodocrud.domain.Todo;

public interface CreateTodoUseCase {
    Todo create(String text, String date);
}
