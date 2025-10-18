package com.langhuan.controller

import com.langhuan.common.Result
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MockController {

    companion object {
        private val log = LoggerFactory.getLogger(MockController::class.java)
    }

    @RequestMapping("/mock")
    fun mock(@RequestBody data: Any): Result<*> {
        return Result.success(data)
    }
}
