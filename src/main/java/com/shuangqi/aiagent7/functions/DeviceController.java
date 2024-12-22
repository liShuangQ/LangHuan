package com.shuangqi.aiagent7.functions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;

@Slf4j
public class DeviceController {
    public void setDeviceState(String deviceId, boolean state, ToolContext context) {
        Map<String, Object> contextData = context.getContext();
        log.debug("deviceId: {}", deviceId);
        log.debug("state: {}", state);
        log.debug("contextData: {}", contextData.get("location"));
        // Implementation using context data
    }
}