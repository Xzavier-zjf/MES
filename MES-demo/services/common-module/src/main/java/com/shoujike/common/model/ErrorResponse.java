package com.shoujike.common.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private int code;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }


    // Getters
}