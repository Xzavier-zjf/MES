package com.shoujike.common.exception;

public class BusinessException extends Exception {
    private final String message;

    public BusinessException(String message) {
        super(message); // 这一步是关键！
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
