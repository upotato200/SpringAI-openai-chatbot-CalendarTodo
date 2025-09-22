package com.best.caltodocrud.application.port.in.summarize;

import jakarta.validation.constraints.NotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SummarizeScheduleCommand {

    private final String from;
    private final String to;
    private final List<Todo> todos;

    public SummarizeScheduleCommand(String from, String to, List<Todo> todos) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.todos = List.copyOf(Objects.requireNonNull(todos));
    }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public List<Todo> getTodos() { return Collections.unmodifiableList(todos); }

    public static class Todo {
        private final String id;
        private final String text;
        private final boolean done;
        private final String date;
        public Todo(@NotBlank String id, String text, boolean done, String date) {
            this.id = id; this.text = text; this.done = done; this.date = date;
        }
        public String getId() { return id; }
        public String getText() { return text; }
        public boolean isDone() { return done; }
        public String getDate() { return date; }
    }
}
