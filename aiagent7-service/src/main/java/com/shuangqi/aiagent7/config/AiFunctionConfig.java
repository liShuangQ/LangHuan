package com.shuangqi.aiagent7.config;

import com.shuangqi.aiagent7.functions.DeviceDemoFunction;
import com.shuangqi.aiagent7.functions.DocumentAnalyzerFunction;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.BiFunction;
import java.util.function.Function;

@Configuration
public class AiFunctionConfig {

    // chat_ 开头为对话函数
    @Bean
    @Description("设置设备的状态")
    public BiFunction<DeviceDemoFunction.Request, ToolContext, DeviceDemoFunction.Response> chat_deviceDemoFunction() {
        return new DeviceDemoFunction();
    }
    @Bean
    @Description("根据文件位置解析")
    public BiFunction<DocumentAnalyzerFunction.Request, ToolContext, DocumentAnalyzerFunction.Response> chat_documentAnalyzerFunction() {
        return new DocumentAnalyzerFunction();
    }

}