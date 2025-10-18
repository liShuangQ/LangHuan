package com.langhuan

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
class LanghuanApplication

fun main(args: Array<String>) {
	runApplication<LanghuanApplication>(*args)
    println("启动成功")
}
