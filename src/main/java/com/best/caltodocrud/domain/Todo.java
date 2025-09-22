package com.best.caltodocrud.domain;

import java.util.Objects;

public class Todo {
    private final Long id;   // DB PK를 Long 타입으로 통일
    private final String text;
    private final boolean done;
    private final String date; // yyyy-MM-dd

    public Todo(Long id, String text, boolean done, String date) {
        this.id = id;
        this.text = Objects.requireNonNull(text);
        this.done = done;
        this.date = Objects.requireNonNull(date);
    }
    public Long getId() { return id; }
    public String getText() { return text; }
    public boolean isDone() { return done; }
    public String getDate() { return date; }
}
