package com.best.caltodocrud.api.common.mapper;

import com.best.caltodocrud.api.summary.dto.SummaryRequestDto;
import com.best.caltodocrud.api.summary.dto.SummaryResponseDto;
import com.best.caltodocrud.api.todo.dto.TodoResponse;
import com.best.caltodocrud.application.port.in.summarize.SummarizeScheduleCommand;
import com.best.caltodocrud.domain.SummaryResult;
import com.best.caltodocrud.domain.Todo;

import java.util.List;

public final class DtoMapper {

    private DtoMapper() {}

    // ===== helpers =====
    private static String toStringId(Object id) {
        return (id == null) ? "" : String.valueOf(id);
    }

    private static Long toLongId(Object id) {
        if (id == null) return null;
        if (id instanceof Long l) return l;
        try {
            return Long.valueOf(String.valueOf(id));
        } catch (NumberFormatException e) {
            return null; // 숫자로 못 바꾸면 null로 둔다 (컨트롤러/서비스에서 처리)
        }
    }

    // =========================
    // Todo (도메인) -> TodoResponse (API DTO)
    //  - API 응답은 Long id 사용
    // =========================
    public static TodoResponse toResponse(Todo t) {
        if (t == null) return null;
        return TodoResponse.builder()
                .id(toLongId(t.getId()))   // ✅ Long으로 변환
                .text(t.getText())
                .done(t.isDone())
                .date(t.getDate())
                .build();
    }

    // =========================
    // 프론트에서 보낸 SummaryRequestDto -> 요약 커맨드
    //  - 커맨드는 String id 사용
    // =========================
    public static SummarizeScheduleCommand toCommand(SummaryRequestDto dto) {
        List<SummarizeScheduleCommand.Todo> todos = dto.getTodos().stream()
                .map(x -> new SummarizeScheduleCommand.Todo(
                        x.getId(),                                  // ✅ String
                        x.getText(),
                        Boolean.TRUE.equals(x.getDone()),
                        x.getDate()
                ))
                .toList();
        return new SummarizeScheduleCommand(dto.getFrom(), dto.getTo(), todos);
    }

    // =========================
    // DB에서 조회한 도메인 Todo 리스트 -> 요약 커맨드
    //  - 커맨드는 String id 사용
    // =========================
    public static SummarizeScheduleCommand toCommandFromDBData(String from, String to, List<Todo> dbTodos) {
        List<SummarizeScheduleCommand.Todo> todos = dbTodos.stream()
                .map(t -> new SummarizeScheduleCommand.Todo(
                        toStringId(t.getId()),   // ✅ Long이든 String이든 문자열로 통일
                        t.getText(),
                        t.isDone(),
                        t.getDate()
                ))
                .toList();
        return new SummarizeScheduleCommand(from, to, todos);
    }

    // =========================
    // 요약 결과 (도메인) -> 응답 DTO
    // =========================
    public static SummaryResponseDto toDto(SummaryResult r) {
        if (r == null) {
            return SummaryResponseDto.builder().build();
        }
        return SummaryResponseDto.builder()
                .title(r.getTitle())
                .oneLine(r.getOneLine())
                .bullets(r.getBullets())
                .riskNote(r.getRiskNote())
                .freeText(r.getFreeText())
                .build();
    }
}
