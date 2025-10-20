package com.langhuan.utils.rerank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 重排模型处理器工厂
 * 根据模型名称创建对应的处理器实例
 */
@Component
public class ReRankProcessorFactory {
    @Value("${rerank.model}")
    private String model;

    private final Map<String, ReRankProcessor> processorMap;

    @Autowired
    public ReRankProcessorFactory(List<ReRankProcessor> processors) {
        // 将所有处理器注册到map中，key为模型名称
        this.processorMap = processors.stream()
                .collect(Collectors.toMap(
                        ReRankProcessor::getModelName,
                        Function.identity()
                ));
    }

    /**
     * 根据模型名称获取对应的处理器
     *
     * @return 对应的处理器实例
     * @throws IllegalArgumentException 如果找不到对应的处理器
     */
    public ReRankProcessor getProcessor() {
        // 支持模糊匹配，如果模型名称包含某个处理器的模型名，则返回该处理器
        for (Map.Entry<String, ReRankProcessor> entry : processorMap.entrySet()) {
            if (model.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException("不支持的重排模型: " + model);
    }

    /**
     * 获取所有支持的模型名称
     *
     * @return 支持的模型名称列表
     */
    public List<String> getSupportedModels() {
        return processorMap.keySet().stream().toList();
    }
}