package com.best.caltodocrud.application.service.todo;

import com.best.caltodocrud.application.port.in.todo.CreateTodoUseCase;
import com.best.caltodocrud.application.port.out.TodoRepositoryPort;
import com.best.caltodocrud.domain.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class CreateTodoService implements CreateTodoUseCase {

    private final TodoRepositoryPort repo;

    @Transactional
    @Override
    public Todo create(String text, String date) {
        Assert.hasText(text, "text must not be empty");
        Assert.hasText(date, "date must not be empty");
        var todo = new Todo(null, text.trim(), false, date);
        return repo.save(todo);
    }
}
