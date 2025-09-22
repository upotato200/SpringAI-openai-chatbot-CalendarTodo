package com.best.caltodocrud.application.service.todo;

import com.best.caltodocrud.api.todo.dto.TodoSyncRequest;
import com.best.caltodocrud.application.port.in.todo.CreateTodoUseCase;
import com.best.caltodocrud.application.port.in.todo.GetTodosUseCase;
import com.best.caltodocrud.application.port.in.todo.UpdateTodoUseCase;
import com.best.caltodocrud.domain.Todo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TodoSyncService {

    private final CreateTodoUseCase createUseCase;
    private final UpdateTodoUseCase updateUseCase;
    private final GetTodosUseCase getTodosUseCase;

    public List<Todo> syncTodos(List<TodoSyncRequest> requests) {
        log.info("Starting sync for {} todos", requests.size());
        
        // 날짜별로 그룹화
        Map<String, List<TodoSyncRequest>> requestsByDate = requests.stream()
                .collect(Collectors.groupingBy(TodoSyncRequest::getDate));
        
        List<Todo> results = new ArrayList<>();
        
        for (Map.Entry<String, List<TodoSyncRequest>> entry : requestsByDate.entrySet()) {
            String date = entry.getKey();
            List<TodoSyncRequest> dateRequests = entry.getValue();
            
            // 해당 날짜의 기존 Todo들을 한 번에 조회
            List<Todo> existingTodos = getTodosUseCase.findByDate(date);
            Map<String, Todo> existingByText = existingTodos.stream()
                    .collect(Collectors.toMap(Todo::getText, todo -> todo));
            
            // 각 요청 처리
            for (TodoSyncRequest request : dateRequests) {
                try {
                    Todo result = processSyncRequest(request, existingByText.get(request.getText()));
                    results.add(result);
                } catch (Exception e) {
                    log.error("Failed to sync todo: {} / {}", request.getDate(), request.getText(), e);
                }
            }
        }
        
        log.info("Successfully synced {} todos", results.size());
        return results;
    }
    
    private Todo processSyncRequest(TodoSyncRequest request, Todo existingTodo) {
        if (existingTodo == null) {
            // 새로 생성
            Todo created = createUseCase.create(request.getText(), request.getDate());
            if (request.isDone()) {
                return updateUseCase.update(created.getId(), created.getText(), true);
            }
            return created;
        } else {
            // 상태 업데이트 필요한지 확인
            if (existingTodo.isDone() != request.isDone()) {
                return updateUseCase.update(existingTodo.getId(), existingTodo.getText(), request.isDone());
            }
            return existingTodo;
        }
    }
}
