package com.best.caltodocrud.api.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ApiError {
    private final String path;
    private final int status;
    private final String code;
    private final String message;
    private final OffsetDateTime timestamp;
}
