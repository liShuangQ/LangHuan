package com.shuangqi.aiagent7.common;

import lombok.Getter;

//throw new BusinessException(400, "Invalid parameter");
//// 或
// throw new BusinessException("Business error message");
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this(500, message);
    }
}
