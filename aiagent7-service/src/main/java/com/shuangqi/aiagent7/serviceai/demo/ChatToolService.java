package com.shuangqi.aiagent7.serviceai.demo;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import com.shuangqi.aiagent7.functionTools.DateTimeToolsD;
import com.shuangqi.aiagent7.functionTools.FileReadTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatToolService {

    private final ChatClient chatClient;

    public ChatToolService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
                .build();
    }

    public String tools(String p) {
        return this.chatClient.prompt(
                        new Prompt(
                                p
                        )
                )
//                .tools(new DateTimeToolsD())
                .tools(ToolCallbacks.from(new DateTimeToolsD(),new FileReadTools()))
                .call()
                .content();
    }

    // 不推荐使用，1.0.0-M6 版本貌似有bug 会导致无限调用 不返回
//    public String DateTimeToolsMethod(String p) {
//        Method method = ReflectionUtils.findMethod(DateTimeToolsD.class, "getCurrentDateTime");
//        ToolCallback toolCallback = MethodToolCallback.builder()
//                .toolDefinition(ToolDefinition.builder(method)
//                        .description("获取用户时区中的当前日期和时间")
//                        .build())
//                .toolMethod(method)
//                .toolObject(new DateTimeToolsD()) //如果方法是静态的，则可以省略该方法，因为它不是必需的。toolObject()
//                .build();
//
//        return this.chatClient.prompt(
//                        new Prompt(
//                                p
//                        )
//                )
//                .tools(toolCallback)
//                .call()
//                .content();
//    }

//    public String WeatherServiceFunction(String p) {
//        ToolCallback toolCallback = FunctionToolCallback
//                .builder("currentWeather", new WeatherService())
//                .description("获取指定位置的天气")
//                .inputType(String.class)
//                .build();
//        return this.chatClient.prompt(
//                        new Prompt(
//                                p
//                        )
//                )
//                .tools(toolCallback)
//                .call()
//                .content();
//    }
//
//    public String WeatherServiceFunctionBeanTools(String p) {
//        return this.chatClient.prompt(
//                        new Prompt(
//                                p)
//                )
//                .tools("currentWeather")
//                .call()
//                .content();
//    }




}
