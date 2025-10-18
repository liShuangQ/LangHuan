package com.langhuan.utils.http

import org.slf4j.LoggerFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

object PostRequestUtils {

	private val log = LoggerFactory.getLogger(PostRequestUtils::class.java)

	/**
	 * 发送POST请求（JSON格式）
	 *
	 * @param url      请求的URL
	 * @param jsonData 请求的JSON数据
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendPostRequest(url: String, jsonData: String): String {
		return sendPostRequest(url, jsonData, null)
	}

	/**
	 * 发送POST请求（JSON格式）
	 *
	 * @param url      请求的URL
	 * @param jsonData 请求的JSON数据
	 * @param headers  请求头
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendPostRequest(url: String, jsonData: String, headers: Map<String, String>?): String {
		log.debug("Sending POST request to URL: $url")
		//        log.debug("Request JSON data: $jsonData")
		//        log.debug("Request headers: $headers")

		val obj: URL = URI(url).toURL()
		val con = obj.openConnection() as HttpURLConnection
		con.requestMethod = "POST"
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
		con.setRequestProperty("Accept", "application/json")
		con.doOutput = true

		// 添加自定义请求头
		headers?.forEach { (key, value) ->
			// 不覆盖Content-Type和Accept
			if (!"Content-Type".equals(key, ignoreCase = true) &&
				!"Accept".equals(key, ignoreCase = true)) {
				con.setRequestProperty(key, value)
			}
		}

		con.outputStream.use { os ->
			val input = jsonData.toByteArray(StandardCharsets.UTF_8)
			os.write(input, 0, input.size)
		}

		return readResponse(con)
	}

	/**
	 * 发送POST请求（form-data格式）
	 *
	 * @param url      请求的URL
	 * @param formData 表单数据
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendPostRequestWithFormData(url: String, formData: Map<String, Any>): String {
		return sendPostRequestWithFormData(url, formData, null)
	}

	/**
	 * 发送POST请求（form-data格式）
	 *
	 * @param url      请求的URL
	 * @param formData 表单数据（支持String和File类型）
	 * @param headers  请求头
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendPostRequestWithFormData(
		url: String,
		formData: Map<String, Any>,
		headers: Map<String, String>?
	): String {
		log.debug("Sending POST request with form-data to URL: $url")
		log.debug("Form data keys: ${formData.keys}")

		val boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "")

		val obj: URL = URI(url).toURL()
		val con = obj.openConnection() as HttpURLConnection
		con.requestMethod = "POST"
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
		con.doOutput = true
		con.doInput = true

		// 添加自定义请求头
		headers?.forEach { (key, value) ->
			// 不覆盖Content-Type
			if (!"Content-Type".equals(key, ignoreCase = true)) {
				con.setRequestProperty(key, value)
			}
		}

		con.outputStream.use { os ->
			PrintWriter(OutputStreamWriter(os, StandardCharsets.UTF_8), true).use { writer ->
				for ((fieldName, fieldValue) in formData) {
					if (fieldValue is File) {
						// 处理文件上传
						val file = fieldValue
						writer.append("--").append(boundary).append("\r\n")
						writer.append("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"${file.name}\"").append("\r\n")
						writer.append("Content-Type: application/octet-stream").append("\r\n")
						writer.append("\r\n")
						writer.flush()

						// 写入文件内容
						FileInputStream(file).use { fis ->
							val buffer = ByteArray(4096)
							var bytesRead: Int
							while (fis.read(buffer).also { bytesRead = it } != -1) {
								os.write(buffer, 0, bytesRead)
							}
							os.flush()
						}
						writer.append("\r\n")
					} else {
						// 处理普通表单字段
						writer.append("--").append(boundary).append("\r\n")
						writer.append("Content-Disposition: form-data; name=\"$fieldName\"").append("\r\n")
						writer.append("\r\n")
						writer.append(fieldValue.toString()).append("\r\n")
					}
				}

				// 结束边界
				writer.append("--").append(boundary).append("--").append("\r\n")
				writer.flush()
			}
		}

		// 读取响应
		return readResponse(con)
	}

	/**
	 * 发送POST请求（application/x-www-form-urlencoded格式）
	 *
	 * @param url      请求的URL
	 * @param formData 表单数据
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendPostRequestWithUrlEncoded(url: String, formData: Map<String, String>): String {
		return sendPostRequestWithUrlEncoded(url, formData, null)
	}

	/**
	 * 发送POST请求（application/x-www-form-urlencoded格式）
	 *
	 * @param url      请求的URL
	 * @param formData 表单数据
	 * @param headers  请求头
	 * @return 响应的数据
	 * @throws Exception 如果请求过程中发生异常
	 */
	@Throws(Exception::class)
	fun sendPostRequestWithUrlEncoded(
		url: String,
		formData: Map<String, String>,
		headers: Map<String, String>?
	): String {
		log.debug("Sending POST request with url-encoded data to URL: $url")
		log.debug("Form data: $formData")

		val obj: URL = URI(url).toURL()
		val con = obj.openConnection() as HttpURLConnection
		con.requestMethod = "POST"
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
		con.doOutput = true

		// 添加自定义请求头
		headers?.forEach { (key, value) ->
			// 不覆盖Content-Type
			if (!"Content-Type".equals(key, ignoreCase = true)) {
				con.setRequestProperty(key, value)
			}
		}

		// 构建URL编码的表单数据
		val postData = StringBuilder()
		for ((key, value) in formData) {
			if (postData.isNotEmpty()) {
				postData.append('&')
			}
			postData.append(java.net.URLEncoder.encode(key, "UTF-8"))
			postData.append('=')
			postData.append(java.net.URLEncoder.encode(value, "UTF-8"))
		}

		con.outputStream.use { os ->
			val input = postData.toString().toByteArray(StandardCharsets.UTF_8)
			os.write(input, 0, input.size)
		}

		return readResponse(con)
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
