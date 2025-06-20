package com.shoujike.common.exception;

public class BusinessException extends  Exception {
    public String getMessage() {
        return null;
    }
    public BusinessException(String message) {
        super(message); // 调用父类的构造函数
    }
}
