package com.best.caltodocrud.application.port.service.summary;

import com.best.caltodocrud.application.port.in.summarize.SummarizeScheduleCommand;
import com.best.caltodocrud.application.port.in.summarize.SummarizeScheduleUseCase;
import com.best.caltodocrud.application.port.out.AiSummaryServicePort;
import com.best.caltodocrud.domain.SummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryService implements SummarizeScheduleUseCase {

    private final AiSummaryServicePort llm; // 조건에 의해 단 하나만 빈 등록됨

    @Override
    public SummaryResult summarize(SummarizeScheduleCommand command) {
        if (command == null || command.getTodos() == null || command.getTodos().isEmpty()) {
            return SummaryResult.builder()
                    .title("일정 요약")
                    .oneLine("해당 기간의 일정이 없습니다.")
                    .bullets("• 일정 없음")
                    .riskNote("특별한 주의사항 없음")
                    .freeText("선택된 기간에 등록된 일정이 없어요.")
                    .build();
        }
        return llm.summarize(command);
    }
}
