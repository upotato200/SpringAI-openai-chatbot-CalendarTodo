package com.best.caltodocrud.application.port.out;

import com.best.caltodocrud.domain.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoRepositoryPort {
    Todo save(Todo todo);
    Optional<Todo> findById(Long id);
    List<Todo> findByDate(String date);
    List<Todo> findRange(String from, String to);
    void deleteById(Long id);
}