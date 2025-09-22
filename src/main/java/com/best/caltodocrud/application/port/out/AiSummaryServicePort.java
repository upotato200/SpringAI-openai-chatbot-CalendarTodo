package com.best.caltodocrud.application.port.out;

import com.best.caltodocrud.application.port.in.summarize.SummarizeScheduleCommand;
import com.best.caltodocrud.domain.SummaryResult;

public interface AiSummaryServicePort {
    SummaryResult summarize(SummarizeScheduleCommand command);
}
