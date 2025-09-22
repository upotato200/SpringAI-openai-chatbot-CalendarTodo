package com.best.caltodocrud.application.port.in.summarize;

import com.best.caltodocrud.domain.SummaryResult;

public interface SummarizeScheduleUseCase {
    SummaryResult summarize(SummarizeScheduleCommand command);
}
