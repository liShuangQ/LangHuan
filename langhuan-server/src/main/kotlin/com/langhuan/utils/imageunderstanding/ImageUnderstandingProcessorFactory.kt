package com.langhuan.utils.imageunderstanding

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.function.Function
import java.util.stream.Collectors

/**
 * 图像理解模型处理器工厂
 * 根据模型名称创建对应的处理器实例
 */
@Component
class ImageUnderstandingProcessorFactory {

    @Value("\${image_understanding.model}")
    private lateinit var model: String

    private val processorMap: Map<String, ImageUnderstandingProcessor>

    @Autowired
    constructor(processors: List<ImageUnderstandingProcessor>) {
        // 将所有处理器注册到map中，key为模型名称
        this.processorMap = processors.stream()
            .collect(Collectors.toMap(
                ImageUnderstandingProcessor::getModelName,
                Function.identity()
            ))
    }

    /**
     * 根据模型名称获取对应的处理器
     *
     * @return 对应的处理器实例
     * @throws IllegalArgumentException 如果找不到对应的处理器
     */
    fun getProcessor(): ImageUnderstandingProcessor {
        // 支持模糊匹配，如果模型名称包含某个处理器的模型名，则返回该处理器
        for ((key, value) in processorMap) {
            if (model.lowercase().contains(key.lowercase())) {
                return value
            }
        }

        throw IllegalArgumentException("不支持的图像理解模型: $model")
    }

    /**
     * 获取所有支持的模型名称
     *
     * @return 支持的模型名称列表
     */
    fun getSupportedModels(): List<String> {
        return processorMap.keys.stream().toList()
    }
}
