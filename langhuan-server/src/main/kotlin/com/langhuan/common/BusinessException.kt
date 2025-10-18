package com.langhuan.common

//throw new BusinessException(400, "Invalid parameter");
//// 或
// throw new BusinessException("Business error message");
class BusinessException : RuntimeException {
    val code: Int
    override val message: String

    constructor(code: Int, message: String) : super(message) {
        this.code = code
        this.message = message
    }

    constructor(message: String) : this(500, message)
}
