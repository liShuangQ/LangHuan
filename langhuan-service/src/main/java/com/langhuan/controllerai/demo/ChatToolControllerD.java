package com.langhuan.controllerai.demo;

import com.langhuan.serviceai.ChatGeneralAssistanceService;
import com.langhuan.serviceai.demo.ChatToolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class ChatToolControllerD {
    private final ChatToolService chatToolService;
    private final ChatGeneralAssistanceService chatGeneralAssistanceService;

    public ChatToolControllerD(ChatToolService chatToolService, ChatGeneralAssistanceService chatGeneralAssistanceService) {
        this.chatToolService = chatToolService;
        this.chatGeneralAssistanceService = chatGeneralAssistanceService;
    }

    @GetMapping("/tools")
    public String tools(@RequestParam String p) {
        // 二次询问ai，弥补因模型问题导致的内部工具调用错误。
        // 由于模型返回的就是字符串，所有这里可能无法使用一个好的方式判断是不是使用模型二次阅读。
        // 这里暂时使用字符串判断，如果模型返回的结果是以***tools***开始，则说明是二次询问ai，否则直接返回。
        String tools = chatToolService.tools(p);
        if (tools.startsWith("***tools***")) {
            return chatGeneralAssistanceService.tools(tools);
        } else {
            return tools;
        }
    }


}
