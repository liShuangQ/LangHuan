package com.langhuan.common;

/**
 * Token过期异常
 */
public class TokenExpiredException extends BaseException {
    
    public TokenExpiredException(String message) {
        super(ResponseCodeEnum.UNAUTHORIZED, message);
    }
    
    public TokenExpiredException() {
        super(ResponseCodeEnum.UNAUTHORIZED, "Token已过期，请重新登录");
    }
}
