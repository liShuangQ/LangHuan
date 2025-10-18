package com.langhuan.utils.imageunderstanding

import cn.hutool.json.JSONUtil
import com.langhuan.utils.http.PostRequestUtils
import com.langhuan.utils.other.ImgUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Qwen-VL 图像理解模型处理器
 * 处理阿里云 Qwen-VL 系列模型的图像理解请求
 */
@Component
class QwenVLProcessor : ImageUnderstandingProcessor {

	companion object {
		// 10MB大小限制
		private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB in bytes
	}

	@Value("\${image_understanding.base-url}")
	private lateinit var baseUrl: String

	@Value("\${image_understanding.api-key}")
	private lateinit var apiKey: String

	@Value("\${image_understanding.model}")
	private lateinit var model: String

	@Throws(Exception::class)
	override fun understandImage(imageUrl: List<String>, prompt: String): String {
		return callApi(imageUrl, prompt)
	}

	@Throws(Exception::class)
	override fun understandImage(imageFile: Array<MultipartFile>, prompt: String): String {
		val base64Urls = mutableListOf<String>()
		for (file in imageFile) {
			if (file.size > MAX_FILE_SIZE) {
				throw IllegalArgumentException("图像文件大小不能超过10MB")
			}
			// 将文件转换为base64编码
			val base64Image = ImgUtil.encodeMultipartFileToBase64(file)

			// 根据文件扩展名确定图片格式
			val imageFormat = ImgUtil.getImageFormat(file)

			// 构造base64 URL
			val base64Url = "data:image/$imageFormat;base64,$base64Image"
			base64Urls.add(base64Url)
		}

		return callApi(base64Urls, prompt)
	}

	/**
	 * 调用Qwen-VL模型API
	 *
	 * @param imageUrl 图像URL或base64编码的图像
	 * @param prompt   提示文本
	 * @return 图像理解结果
	 * @throws Exception 处理过程中的异常
	 */
	@Throws(Exception::class)
	private fun callApi(imageUrl: List<String>, prompt: String): String {
		// 构建请求内容
		val contentList = mutableListOf<Map<String, Any>>()

		// 添加图像内容
		for (url in imageUrl) {
			val imageContent = mapOf(
				"type" to "image_url",
				"image_url" to mapOf("url" to url)
			)
			contentList.add(imageContent)
		}

		// 添加文本提示
		val textContent = mapOf(
			"type" to "text",
			"text" to prompt
		)
		contentList.add(textContent)

		// 构建消息
		val message = mapOf(
			"role" to "user",
			"content" to contentList
		)

		// 构建请求数据
		val jsonData = JSONUtil.toJsonStr(
			mapOf(
				"model" to model,
				"messages" to listOf(message),
				"stream" to false,
				// enable_thinking 参数开启思考过程，thinking_budget 参数设置最大推理过程 Token 数
				"extra_body" to mapOf(
					"enable_thinking" to false,
					"thinking_budget" to 81920
				)
			)
		)

		// 发送 POST 请求
		val response = PostRequestUtils.sendPostRequest(
			baseUrl, jsonData,
			mapOf("Authorization" to "Bearer $apiKey", "Content-Type" to "application/json")
		)

		// 解析响应并返回结果
		return try {
			val qwenResponse = JSONUtil.toBean(response, QwenVLResponse::class.java)
			val out = StringBuilder()
			qwenResponse.choices?.forEach { choice ->
				choice.message?.content?.let { content ->
					out.append(content).append("\n")
				}
			}
			out.toString()
		} catch (e: Exception) {
			// 如果标准转换失败，尝试手动解析JSON
			try {
				val jsonObject = JSONUtil.parseObj(response)
				val choicesArray = jsonObject.getJSONArray("choices")
				val out = StringBuilder()
				for (i in 0 until choicesArray.size) {
					val choice = choicesArray.getJSONObject(i)
					val message = choice.getJSONObject("message")
					val content = message.getStr("content")
					out.append(content).append("\n")
				}
				out.toString()
			} catch (ex: Exception) {
				throw Exception("JSON解析失败: ${ex.message}, 原始响应: $response", ex)
			}
		}
	}

	override fun getModelName(): String {
		return "qwen-vl"
	}

	/**
	 * Qwen-VL 模型响应结果类
	 */
	class QwenVLResponse {
		var choices: List<Choice>? = null
		var `object`: String? = null
		var usage: Usage? = null
		var created: Long? = null
		var system_fingerprint: String? = null
		var model: String? = null
		var id: String? = null

		class Choice {
			var message: Message? = null
			var finish_reason: String? = null
			var index: Int? = null
			var logprobs: Any? = null

			class Message {
				var content: String? = null
				var role: String? = null
			}
		}

		class Usage {
			var prompt_tokens: Int? = null
			var completion_tokens: Int? = null
			var total_tokens: Int? = null
			var prompt_tokens_details: PromptTokensDetails? = null

			class PromptTokensDetails {
				var cached_tokens: Int? = null
			}
		}
	}
}
