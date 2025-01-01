package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.config.AiFunctionConfig;
import com.shuangqi.aiagent7.functions.DeviceDemoMethod;
import com.shuangqi.aiagent7.functions.MockWeatherService;
import com.shuangqi.aiagent7.functions.SearchLocationNameFunction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
public class ChatFunctionService {

    private final ChatClient chatClient;
    private ConfigurationClassPostProcessor configurationClassPostProcessor;

    public ChatFunctionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem("""
                        回答我的问题。
                        返回如下格式:
                        {desc: document}
                        """)
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
                .build();
    }

    public String setDeviceStatusFunction(String q) {
        return this.chatClient.prompt()
                .user(q)
                // 直接创建，无需bean
//                .functions(FunctionCallback.builder()
//                        .description("设置设备的状态")
//                        .function("DeviceDemoFunction", new DeviceDemoFunction())
//                        .inputType(DeviceDemoFunction.Request.class)
//                        .build()
//                )
                // 使用bean，当多个函数的时候，ai自己选择最合适的
                .functions("deviceDemoFunction", "documentAnalyzerFunction")
                .toolContext(Map.of("location", "home"))
                .call()
                .content();
    }

    public String setDeviceStatusMethod(String q) {
        return this.chatClient.prompt("返回成功还是失败信息")
                .user(q)
                .functions(FunctionCallback.builder()
                        .description("Control device state")
                        .method("setDeviceState", String.class, boolean.class, ToolContext.class)
                        .targetObject(new DeviceDemoMethod())
                        .build())
                .toolContext(Map.of("location", "home"))
                .call()
                .content();
    }


    public String searchLocationNameFunction() {
        return this.chatClient.prompt()
                .user("辽宁有几个叫黄晓明的人？")
                .functions(FunctionCallback.builder()
                        .description("地市中有多少个姓名相同的人")
                        .function("SearchLocationNameFunction", new SearchLocationNameFunction())
                        .inputType(SearchLocationNameFunction.Request.class)
                        .build())
//                .functions("deviceFunction")
                .toolContext(Map.of("id", "1"))
                .call()
                .content();

    }


    public String mockWeatherService() {
        return this.chatClient.prompt()
                .user("What's the weather like in San Francisco, Tokyo, and Paris?")
                .functions(FunctionCallback.builder()
                        .description("Get the weather in location")
                        .function("MockWeatherService", new MockWeatherService())
                        .inputType(MockWeatherService.Request.class)
                        .build())
                .toolContext(Map.of("id", "1"))
                .call()
                .content();

    }


    public String readFile(String prompt) {

        return this.chatClient.prompt()
                .messages(new UserMessage(prompt))
                // spring ai会从已注册为bean的function中查找函数，将它添加到请求中。如果成功触发就会调用函数
                .functions("documentAnalyzerFunction")
                .call()
                .content();
    }



}
