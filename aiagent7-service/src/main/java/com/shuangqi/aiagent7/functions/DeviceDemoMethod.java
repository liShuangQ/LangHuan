package com.shuangqi.aiagent7.functions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Description;

import java.util.Map;

@Slf4j
public class DeviceDemoMethod {
    @Description("设置设备的状态")
    public void setDeviceState(String deviceId, boolean state, ToolContext context) {
        Map<String, Object> contextData = context.getContext();
        log.debug("deviceId: {}", deviceId);
        log.debug("state: {}", state);
        log.debug("contextData: {}", contextData.get("location"));
    }
}