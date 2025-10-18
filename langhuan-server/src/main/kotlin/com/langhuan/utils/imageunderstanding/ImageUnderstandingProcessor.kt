package com.langhuan.utils.imageunderstanding

import org.springframework.web.multipart.MultipartFile

/**
 * 图像理解模型处理器接口
 * 定义了所有图像理解模型处理器的标准接口
 */
interface ImageUnderstandingProcessor {

    /**
     * 执行图像理解操作
     *
     * @param imageUrl 图像URL
     * @param prompt   提示文本
     * @return 图像理解结果
     * @throws Exception 处理过程中的异常
     */
    @Throws(Exception::class)
    fun understandImage(imageUrl: List<String>, prompt: String): String

    /**
     * 执行图像理解操作（文件上传方式）
     *
     * @param imageFile 图像文件
     * @param prompt    提示文本
     * @return 图像理解结果
     * @throws Exception 处理过程中的异常
     */
    @Throws(Exception::class)
    fun understandImage(imageFile: Array<MultipartFile>, prompt: String): String

    /**
     * 获取该处理器支持的模型名称
     *
     * @return 模型名称
     */
    fun getModelName(): String
}
