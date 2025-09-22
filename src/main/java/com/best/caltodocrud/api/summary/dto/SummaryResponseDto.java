package com.best.caltodocrud.api.summary.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class SummaryResponseDto {
    private String title;
    private String oneLine;
    private String bullets;
    private String riskNote;
    private String freeText; // 🆕 서술형
}
