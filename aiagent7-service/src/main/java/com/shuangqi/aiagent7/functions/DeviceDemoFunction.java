package com.shuangqi.aiagent7.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;

@Slf4j
@Description("设置设备的状态")
public class DeviceDemoFunction implements BiFunction<DeviceDemoFunction.Request, ToolContext, DeviceDemoFunction.Response> {

    @Override
    public Response apply(Request request, ToolContext context) {
        log.debug("DeviceDemoFunction request.device: {}", request.device);
        log.debug("DeviceDemoFunction request.state: {}", request.state);
        Map<String, Object> contextData = context.getContext();
        log.debug("DeviceDemoFunction context: {}", contextData.get("location"));
        Random random = new Random();
        String response = random.nextBoolean() ? "设置成功。" : "设置失败。";
        log.debug("DeviceDemoFunction response: {}", response);
        return new Response(response);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("匹配设备和状态")
    public record Request(
            @JsonProperty(required = true, value = "device") @JsonPropertyDescription("设备信息device") String device,
            @JsonProperty(required = true, value = "state") @JsonPropertyDescription("状态state") Boolean state
    ) {
    }

    public record Response(String status) {
    }

}
