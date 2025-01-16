package com.shuangqi.aiagent7.config;

import com.shuangqi.aiagent7.functions.DeviceDemoFunction;
import com.shuangqi.aiagent7.functions.DocumentAnalyzerFunction;
import com.shuangqi.aiagent7.functions.HttpRequestFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.BiFunction;

@Slf4j
@Configuration
public class AiChatFunctionConfig {

    // chat_ 开头为对话函数
    @Bean
    @Description("设置设备的状态")
    public BiFunction<DeviceDemoFunction.Request, ToolContext, DeviceDemoFunction.Response> chat_deviceDemoFunction() {
        return new DeviceDemoFunction();
    }

    @Bean
    @Description("根据文件位置解析总结文档")
    public BiFunction<DocumentAnalyzerFunction.Request, ToolContext, DocumentAnalyzerFunction.Response> chat_documentAnalyzerFunction() {
        log.debug("chat_documentAnalyzerFunction");
        return new DocumentAnalyzerFunction();
    }

//    @Bean
//    @Description("根据接口地址URL和请求参数JSON请求接口得到结果")
//    public BiFunction<HttpRequestFunction.Request, ToolContext, HttpRequestFunction.Response> chat_httpRequestFunction() {
//        return new HttpRequestFunction();
//    }

}