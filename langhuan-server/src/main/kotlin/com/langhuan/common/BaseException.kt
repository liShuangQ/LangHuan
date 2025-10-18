package com.langhuan.common

open class BaseException : RuntimeException {
    var responseCode: ResponseCodeEnum? = null
        private set

    constructor(responseCode: ResponseCodeEnum, message: String) : super(message) {
        this.responseCode = responseCode
    }
}
