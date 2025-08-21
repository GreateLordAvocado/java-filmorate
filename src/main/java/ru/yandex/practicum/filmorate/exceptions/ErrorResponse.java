package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorResponse {
    private final String error;
    private final Map<String, String> details;

    public ErrorResponse(String error) {
        this.error = error;
        this.details = null;
    }

    public ErrorResponse(String error, Map<String, String> details) {
        this.error = error;
        this.details = details;
    }
}