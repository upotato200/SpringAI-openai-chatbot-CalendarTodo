package com.best.caltodocrud.application.service.todo;

import com.best.caltodocrud.application.port.in.todo.UpdateTodoUseCase;
import com.best.caltodocrud.application.port.out.TodoRepositoryPort;
import com.best.caltodocrud.domain.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class UpdateTodoService implements UpdateTodoUseCase {

    private final TodoRepositoryPort repo;

    @Transactional
    @Override
    public Todo update(Long id, String text, Boolean done) {
        Assert.notNull(id, "id must not be null");
        Assert.hasText(text, "text must not be empty");
        Assert.notNull(done, "done must not be null");

        var cur = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found: id=" + id));

        var updated = new Todo(cur.getId(), text.trim(), done, cur.getDate());
        return repo.save(updated);
    }
}
