package com.langhuan.model.dto

import java.io.Serializable

data class UserLoginDTO(
    val username: String,
    val password: String
) : Serializable {
    companion object {
        private const val serialVersionUID = -1L
    }
}
