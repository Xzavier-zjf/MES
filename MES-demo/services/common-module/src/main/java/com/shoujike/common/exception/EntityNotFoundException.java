package com.shoujike.common.exception;

public class EntityNotFoundException extends Exception {
    public String getMessage() {
        return null;
    }
    public EntityNotFoundException(String message) {
        super(message); // 调用父类的构造函数
    }
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
