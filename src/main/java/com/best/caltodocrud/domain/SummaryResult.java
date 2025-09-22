package com.best.caltodocrud.domain;

import lombok.*;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class SummaryResult {
    private String title;     // 카드 타이틀
    private String oneLine;   // 한 줄 요약
    private String bullets;   // 불릿(줄바꿈 \n)
    private String riskNote;  // 주의/리스크
    private String freeText;  // 🆕 서술형 요약(그대로 보여줄 텍스트)
}
