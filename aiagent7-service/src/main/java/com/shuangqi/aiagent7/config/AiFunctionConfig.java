package com.shuangqi.aiagent7.config;

import com.shuangqi.aiagent7.functions.DeviceDemoFunction;
import com.shuangqi.aiagent7.functions.DocumentAnalyzerFunction;
import com.shuangqi.aiagent7.functions.HttpRequestFunction;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.BiFunction;

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

    @Bean
    @Description("""
            根据接口地址和请求参数得到结果。
            """)
    public BiFunction<HttpRequestFunction.Request, ToolContext, HttpRequestFunction.Response> chat_httpRequestFunction() {
        return new HttpRequestFunction();
    }

}