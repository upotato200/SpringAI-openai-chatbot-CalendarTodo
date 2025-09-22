package com.best.caltodocrud.infrastructure.persistence.jpa;

import com.best.caltodocrud.application.port.out.TodoRepositoryPort;
import com.best.caltodocrud.domain.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TodoJpaAdapter implements TodoRepositoryPort {

    private final SpringDataTodoRepository repo;

    @Override
    public Todo save(Todo t) {
        return TodoEntityMapper.toDomain(repo.save(TodoEntityMapper.toEntity(t)));
    }

    @Override
    public Optional<Todo> findById(Long id) {
        return repo.findById(id).map(TodoEntityMapper::toDomain);
    }

    @Override
    public List<Todo> findByDate(String date) {
        return repo.findByDateOrderByIdAsc(date).stream().map(TodoEntityMapper::toDomain).toList();
    }

    @Override
    public List<Todo> findRange(String from, String to) {
        return repo.findRange(from, to).stream().map(TodoEntityMapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
