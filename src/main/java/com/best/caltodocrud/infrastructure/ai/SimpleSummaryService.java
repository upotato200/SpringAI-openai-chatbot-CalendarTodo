package com.best.caltodocrud.infrastructure.ai;

import com.best.caltodocrud.application.port.in.summarize.SummarizeScheduleCommand;
import com.best.caltodocrud.application.port.out.AiSummaryServicePort;
import com.best.caltodocrud.domain.SummaryResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SimpleSummaryService implements AiSummaryServicePort {

    @Override
    public SummaryResult summarize(SummarizeScheduleCommand cmd) {
        var todos = cmd.getTodos();
        var openCnt = (int) todos.stream().filter(t -> !t.isDone()).count();
        var doneCnt = todos.size() - openCnt;

        String top = todos.stream()
                .sorted(Comparator.comparing(SummarizeScheduleCommand.Todo::isDone)
                        .thenComparing(SummarizeScheduleCommand.Todo::getText))
                .limit(3)
                .map(SummarizeScheduleCommand.Todo::getText)
                .collect(Collectors.joining(", "));

        String bullets = todos.stream()
                .map(t -> (t.isDone() ? "• [완료] " : "• [진행] ") + t.getText())
                .collect(Collectors.joining("\n"));

        String oneLine = String.format("미완료 %d건 · 완료 %d건 — 대표: %s", openCnt, doneCnt, top);
        String free = String.format(
                "%s부터 %s까지 일정은 총 %d건이에요. 미완료 %d건, 완료 %d건이에요. 대표 일정은 %s 입니다.",
                cmd.getFrom(), cmd.getTo(), todos.size(), openCnt, doneCnt, top
        );

        return SummaryResult.builder()
                .title("일정 요약")
                .oneLine(oneLine)
                .bullets(bullets)
                .riskNote("특별한 주의사항 없음")
                .freeText(free)
                .build();
    }
}
