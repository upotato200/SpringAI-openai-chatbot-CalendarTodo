package com.best.caltodocrud.infrastructure.ai;

import com.best.caltodocrud.application.port.in.summarize.SummarizeScheduleCommand;
import com.best.caltodocrud.application.port.out.AiSummaryServicePort;
import com.best.caltodocrud.domain.SummaryResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class OpenAiChatClientAdapter implements AiSummaryServicePort {

    private final ChatClient chat;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.ai.locale:ko}")
    private String locale;

    @Override
    public SummaryResult summarize(SummarizeScheduleCommand cmd) {
        try {
            String todosBlock = cmd.getTodos().stream()
                    .map(t -> String.format("- [%s] %s (%s)",
                            t.isDone() ? "x" : " ", t.getText(), t.getDate()))
                    .collect(Collectors.joining("\n"));

            String system = """
                    당신은 한국어로 일정을 요약해주는 AI 어시스턴트입니다.
                    
                    반드시 다음 JSON 형식으로만 응답해주세요:
                    {
                      "title": "요약 제목 (한국어)",
                      "oneLine": "한 줄 요약 (한국어)",
                      "bullets": "세부사항 (\\n으로 구분된 한국어 불릿 포인트)",
                      "riskNote": "주의사항 (없으면 '특별한 주의사항 없음')",
                      "freeText": "자연스러운 2-4문장 서술형 요약 (한국어)"
                    }
                    
                    규칙:
                    - 오직 JSON 객체만 출력하세요
                    - 모든 값은 한국어로 작성하세요
                    - 줄바꿈은 \\n 사용하세요
                    - 간결하고 실용적으로 작성하세요
                    """;

            String user = """
                    기간: %s ~ %s
                    일정:
                    %s
                    """.formatted(cmd.getFrom(), cmd.getTo(), todosBlock);

            String raw = chat
                    .prompt()
                    .system(system)
                    .user(user)
                    .call()
                    .content();

            String json = extractJson(raw);
            Payload p = om.readValue(json, Payload.class);

            return SummaryResult.builder()
                    .title(z(p.title, "일정 요약"))
                    .oneLine(z(p.oneLine, "핵심 일정만 간단히 정리했어요."))
                    .bullets(z(p.bullets, "• 요약 정보가 충분하지 않습니다."))
                    .riskNote(z(p.riskNote, "특별한 주의사항 없음"))
                    .freeText(z(p.freeText, p.oneLine))
                    .build();

        } catch (Exception e) {
            log.warn("LLM summarize failed, fallback to minimal summary", e);
            String first = cmd.getTodos().isEmpty() ? "" : cmd.getTodos().get(0).getText();
            return SummaryResult.builder()
                    .title("일정 요약")
                    .oneLine(first.isBlank() ? "요약할 일정이 적습니다." : "주요 일정: " + first + " 등")
                    .bullets(first.isBlank() ? "• 일정이 충분치 않습니다." : "• " + first)
                    .riskNote("특별한 주의사항 없음")
                    .freeText(first.isBlank()
                            ? "선택된 기간에 일정이 충분하지 않아요."
                            : "간단히 정리했어요. 대표 일정은 '" + first + "' 입니다.")
                    .build();
        }
    }

    /** JSON 추출 및 정리 */
    private static String extractJson(String content) {
        if (content == null || content.trim().isEmpty()) return "{}";

        String cleaned = content.trim()
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("```\\s*$", "")
                .trim();
        
        // JSON 객체 추출 시도
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        
        // JSON이 아닌 경우 기본 구조로 래핑
        return String.format(
            "{\"title\":\"일정 요약\",\"oneLine\":\"%s\",\"bullets\":\"• %s\",\"riskNote\":\"특별한 주의사항 없음\",\"freeText\":\"%s\"}", 
            cleaned.replace("\"", "\\\""), 
            cleaned.replace("\"", "\\\""), 
            cleaned.replace("\"", "\\\"")
        );
    }

    private static String z(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Payload {
        public String title;
        public String oneLine;
        public String bullets;
        public String riskNote;
        public String freeText;
    }
}
