package com.best.caltodocrud.application.port.in.todo;

import com.best.caltodocrud.domain.Todo;

import java.util.List;

public interface GetTodosUseCase {
    List<Todo> findByDate(String date);
    List<Todo> findRange(String from, String to);
}
