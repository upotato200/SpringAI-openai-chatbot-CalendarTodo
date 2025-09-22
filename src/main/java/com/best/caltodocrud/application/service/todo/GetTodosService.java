package com.best.caltodocrud.application.service.todo;

import com.best.caltodocrud.application.port.in.todo.GetTodosUseCase;
import com.best.caltodocrud.application.port.out.TodoRepositoryPort;
import com.best.caltodocrud.domain.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTodosService implements GetTodosUseCase {

    private final TodoRepositoryPort repo;

    @Transactional(readOnly = true)
    @Override
    public List<Todo> findByDate(String date) {
        Assert.hasText(date, "date must not be empty");
        return repo.findByDate(date);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Todo> findRange(String from, String to) {
        Assert.hasText(from, "from must not be empty");
        Assert.hasText(to, "to must not be empty");
        return repo.findRange(from, to);
    }
}
