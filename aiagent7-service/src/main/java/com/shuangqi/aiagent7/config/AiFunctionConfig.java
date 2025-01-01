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

}