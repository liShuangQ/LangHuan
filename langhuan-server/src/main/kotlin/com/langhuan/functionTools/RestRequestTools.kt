package com.langhuan.functionTools

import com.langhuan.utils.http.GetRequestUtils
import com.langhuan.utils.http.PostRequestUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam

object RestRequestTools {

    private val log = LoggerFactory.getLogger(RestRequestTools::class.java)

    @Tool(description = "get方法请求rest接口", returnDirect = true)
    @JvmStatic
    @Throws(Exception::class)
    fun sendGetRequest(@ToolParam(description = "接口请求地址url") url: String): String {
        log.info("调用工具：sendGetRequest")
        return try {
            GetRequestUtils.sendGetRequest(url)
        } catch (e: Exception) {
            log.error("sendGetRequest error:{}", e)
            throw e
        }
    }

    @Tool(description = "post方法请求rest接口", returnDirect = true)
    @JvmStatic
    @Throws(Exception::class)
    fun sendPostRequest(
        @ToolParam(description = "接口请求地址url") url: String,
        @ToolParam(description = "接口请求携带数据体") jsonData: String
    ): String {
        log.info("调用工具：sendPostRequest")
        return try {
            PostRequestUtils.sendPostRequest(url, jsonData)
        } catch (e: Exception) {
            log.error("sendPostRequest error:{}", e)
            throw e
        }
    }
}
