package com.shuangqi.aiagent7.config;

import com.shuangqi.aiagent7.functions.DeviceDemoFunction;
import com.shuangqi.aiagent7.functions.DocumentAnalyzerFunction;
import com.shuangqi.aiagent7.functions.SearchLocationNameFunction;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.BiFunction;
import java.util.function.Function;

@Configuration
public class AiFunctionConfig {
    @Bean
    @Description("设置设备的状态")
    public BiFunction<DeviceDemoFunction.Request, ToolContext, DeviceDemoFunction.Response> deviceDemoFunction() {
        return new DeviceDemoFunction();
    }

    @Bean
    @Description("根据文件位置的文档解析函数")
    public Function<DocumentAnalyzerFunction.Request, DocumentAnalyzerFunction.Response> documentAnalyzerFunction() {
        return new DocumentAnalyzerFunction();
    }


    @Bean
    @Description("地市有多少个姓名相同的人")
    public BiFunction<SearchLocationNameFunction.Request, ToolContext, SearchLocationNameFunction.Response> searchLocationNameFunction() {
        return new SearchLocationNameFunction();
    }


}
