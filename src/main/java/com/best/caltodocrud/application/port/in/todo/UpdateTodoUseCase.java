package com.best.caltodocrud.application.port.in.todo;

import com.best.caltodocrud.domain.Todo;

public interface UpdateTodoUseCase {
    Todo update(Long id, String text, Boolean done);
}
