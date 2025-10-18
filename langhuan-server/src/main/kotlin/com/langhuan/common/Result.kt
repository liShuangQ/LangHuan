package com.langhuan.common

data class Result<T>(
    var code: Int? = null,
    var message: String? = null,
    var data: T? = null
) {
    companion object {
        fun <T> success(data: T): Result<T> {
            return Result(200, "success", data)
        }

        fun <T> error(code: Int, message: String): Result<T> {
            return Result(code, message, null)
        }

        fun <T> error(message: String): Result<T> {
            return Result(777, message, null)
        }
    }
}
