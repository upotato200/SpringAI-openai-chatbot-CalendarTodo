package com.best.caltodocrud.api.summary;

import com.best.caltodocrud.api.common.mapper.DtoMapper;
import com.best.caltodocrud.api.summary.dto.SummaryRequestDto;
import com.best.caltodocrud.api.summary.dto.SummaryResponseDto;
import com.best.caltodocrud.application.port.in.summarize.SummarizeScheduleUseCase;
import com.best.caltodocrud.application.port.in.todo.CreateTodoUseCase;
import com.best.caltodocrud.application.port.in.todo.GetTodosUseCase;
import com.best.caltodocrud.domain.Todo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080","http://localhost:5173",
        "http://localhost:63342","http://127.0.0.1:63342",
        "http://localhost:5500","http://127.0.0.1:5500"
})
@Slf4j
public class SummaryController {

    private final SummarizeScheduleUseCase summarizeUseCase;
    private final GetTodosUseCase getTodosUseCase;
    private final CreateTodoUseCase createTodoUseCase;

    @PostMapping("/summary")
    public SummaryResponseDto summarize(@RequestBody @Valid SummaryRequestDto body) {
        int incoming = body.getTodos() == null ? 0 : body.getTodos().size();
        log.info("Summary request: {} ~ {}, incoming todos: {}", body.getFrom(), body.getTo(), incoming);

        if (incoming > 0) syncClientDataToDb(body);

        var dbTodos = getTodosFromDb(body.getFrom(), body.getTo());
        var cmd = DtoMapper.toCommandFromDBData(body.getFrom(), body.getTo(), dbTodos);
        return DtoMapper.toDto(summarizeUseCase.summarize(cmd));
    }

    @GetMapping("/summary/range")
    public SummaryResponseDto summarizeByDb(@RequestParam String from, @RequestParam String to) {
        var dbTodos = getTodosFromDb(from, to);
        var cmd = DtoMapper.toCommandFromDBData(from, to, dbTodos);
        return DtoMapper.toDto(summarizeUseCase.summarize(cmd));
    }

    private void syncClientDataToDb(SummaryRequestDto body) {
        Map<String, List<SummaryRequestDto.TodoItemDto>> byDate = body.getTodos().stream()
                .collect(Collectors.groupingBy(SummaryRequestDto.TodoItemDto::getDate));

        byDate.forEach((date, items) -> {
            try {
                List<Todo> existing = getTodosUseCase.findByDate(date);
                Set<String> texts = existing.stream().map(Todo::getText).collect(Collectors.toSet());
                for (var c : items) {
                    if (!texts.contains(c.getText())) {
                        createTodoUseCase.create(c.getText(), date);
                    }
                }
            } catch (Exception e) {
                log.warn("Sync fail for date={}", date, e);
            }
        });
    }

    private List<Todo> getTodosFromDb(String from, String to) {
        try {
            if (Objects.equals(from, to)) return getTodosUseCase.findByDate(from);
            return getTodosUseCase.findRange(from, to);
        } catch (Exception e) {
            log.error("DB fetch failed", e);
            return List.of();
        }
    }
}
