package com.best.caltodocrud.api.todo;

import com.best.caltodocrud.api.common.mapper.DtoMapper;
import com.best.caltodocrud.api.todo.dto.*;
import com.best.caltodocrud.application.port.in.todo.*;
import com.best.caltodocrud.application.service.todo.TodoSyncService;
import com.best.caltodocrud.domain.Todo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Slf4j
public class TodoController {

    private final CreateTodoUseCase createUC;
    private final UpdateTodoUseCase updateUC;
    private final DeleteTodoUseCase deleteUC;
    private final GetTodosUseCase getUC;
    private final TodoSyncService todoSyncService;

    @PostMapping
    public TodoResponse create(@RequestBody @Valid TodoCreateRequest req) {
        log.info("Creating todo: text={}, date={}", req.getText(), req.getDate());
        Todo out = createUC.create(req.getText(), req.getDate());
        return DtoMapper.toResponse(out);
    }

    @GetMapping
    public List<TodoResponse> findByDate(@RequestParam String date) {
        log.info("Finding todos by date: {}", date);
        var todos = getUC.findByDate(date).stream().map(DtoMapper::toResponse).toList();
        log.info("Found {} todos for date {}", todos.size(), date);
        return todos;
    }

    @GetMapping("/range")
    public List<TodoResponse> range(@RequestParam String from, @RequestParam String to) {
        log.info("Finding todos in range: {} to {}", from, to);
        var todos = getUC.findRange(from, to).stream().map(DtoMapper::toResponse).toList();
        log.info("Found {} todos in range", todos.size());
        return todos;
    }

    @PutMapping("/{id}")
    public TodoResponse update(@PathVariable Long id, @RequestBody @Valid TodoUpdateRequest req) {
        log.info("Updating todo: id={}, text={}, done={}", id, req.getText(), req.getDone());
        Todo out = updateUC.update(id, req.getText(), req.getDone());
        return DtoMapper.toResponse(out);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Deleting todo: id={}", id);
        deleteUC.delete(id);
    }

    @PostMapping("/sync")
    public List<TodoResponse> syncTodos(@RequestBody @Valid List<TodoSyncRequest> requests) {
        log.info("Syncing {} todos from frontend", requests.size());
        var todos = todoSyncService.syncTodos(requests);
        return todos.stream().map(DtoMapper::toResponse).toList();
    }
}
