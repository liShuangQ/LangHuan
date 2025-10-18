package com.langhuan.common

/**
 * Token过期异常
 */
class TokenExpiredException : BaseException {
    
    constructor(message: String) : super(ResponseCodeEnum.UNAUTHORIZED, message)
    
    constructor() : super(ResponseCodeEnum.UNAUTHORIZED, "Token已过期，请重新登录")
}
