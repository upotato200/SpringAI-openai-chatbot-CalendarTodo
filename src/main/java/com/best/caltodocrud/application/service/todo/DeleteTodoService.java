package com.best.caltodocrud.application.service.todo;

import com.best.caltodocrud.application.port.in.todo.DeleteTodoUseCase;
import com.best.caltodocrud.application.port.out.TodoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTodoService implements DeleteTodoUseCase {

    private final TodoRepositoryPort repo;

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
