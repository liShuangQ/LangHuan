package com.langhuan.utils.http

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * GET请求工具类
 * 提供发送GET请求的各种方法，支持请求头和URL参数
 *
 * @author LangHuan
 */
object GetRequestUtils {

	private val log = LoggerFactory.getLogger(GetRequestUtils::class.java)

	/**
	 * 发送GET请求
	 *
	 * @param url 请求的URL
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendGetRequest(url: String): String {
		return sendGetRequest(url, null, null)
	}

	/**
	 * 发送GET请求
	 *
	 * @param url     请求的URL
	 * @param headers 请求头
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendGetRequest(url: String, headers: Map<String, String>?): String {
		return sendGetRequest(url, null, headers)
	}

	/**
	 * 发送GET请求
	 *
	 * @param url    请求的URL
	 * @param params URL参数
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendGetRequestWithParams(url: String, params: Map<String, String>?): String {
		return sendGetRequest(url, params, null)
	}

	/**
	 * 发送GET请求
	 *
	 * @param url     请求的URL
	 * @param params  URL参数
	 * @param headers 请求头
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendGetRequest(url: String, params: Map<String, String>?, headers: Map<String, String>?): String {
		log.debug("Sending GET request to URL: $url")
		log.debug("Request params: $params")
		log.debug("Request headers: $headers")

		// 构建完整的URL（包含参数）
		val fullUrl = buildUrlWithParams(url, params)
		log.debug("Full URL with params: $fullUrl")

		val obj: URL = URI(fullUrl).toURL()
		val con = obj.openConnection() as HttpURLConnection
		con.requestMethod = "GET"
		con.setRequestProperty("Accept", "application/json")
		con.setRequestProperty("User-Agent", "Java-HttpURLConnection/1.0")

		// 添加自定义请求头
		headers?.forEach { (key, value) ->
			con.setRequestProperty(key, value)
		}

		return readResponse(con)
	}

	/**
	 * 构建带参数的URL
	 *
	 * @param url    基础URL
	 * @param params URL参数
	 * @return 完整的URL
	 * @throws Exception 如果URL编码过程中发生异常
	 */
	@Throws(Exception::class)
	private fun buildUrlWithParams(url: String, params: Map<String, String>?): String {
		if (params.isNullOrEmpty()) {
			return url
		}

		val urlBuilder = StringBuilder(url)
		
		// 检查URL是否已经包含参数
		if (url.contains("?")) {
			urlBuilder.append("&")
		} else {
			urlBuilder.append("?")
		}

		// 添加参数
		var first = true
		for ((key, value) in params) {
			if (!first) {
				urlBuilder.append("&")
			}
			urlBuilder.append(java.net.URLEncoder.encode(key, "UTF-8"))
				.append("=")
				.append(java.net.URLEncoder.encode(value.toString(), "UTF-8"))
			first = false
		}

		return urlBuilder.toString()
	}

	/**
	 * 读取HTTP响应
	 *
	 * @param con HTTP连接
	 * @return 响应内容
	 * @throws Exception 如果读取过程中发生异常
	 */
	@Throws(Exception::class)
	private fun readResponse(con: HttpURLConnection): String {
		return try {
			BufferedReader(
				InputStreamReader(con.inputStream, StandardCharsets.UTF_8)
			).use { br ->
				val response = StringBuilder()
				var responseLine: String?
				while (br.readLine().also { responseLine = it } != null) {
					response.append(responseLine!!.trim())
				}
				response.toString()
			}
		} catch (e: Exception) {
			log.error("Error reading response: ${e.message}")
			// 尝试读取错误流
			try {
				BufferedReader(
					InputStreamReader(con.errorStream, StandardCharsets.UTF_8)
				).use { br ->
					val errorResponse = StringBuilder()
					var responseLine: String?
					while (br.readLine().also { responseLine = it } != null) {
						errorResponse.append(responseLine!!.trim())
					}
					log.error("Error response: ${errorResponse.toString()}")
				}
			} catch (ignored: Exception) {
				// 忽略读取错误流时的异常
			}
			throw e
		}
	}
}
