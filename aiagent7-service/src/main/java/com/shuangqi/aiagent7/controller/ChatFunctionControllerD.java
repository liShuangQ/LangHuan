package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.serviceai.ChatFunctionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatFunction")
public class ChatFunctionControllerD {
    private final ChatFunctionService chatFunctionService;

    public ChatFunctionControllerD(ChatFunctionService chatFunctionService) {
        this.chatFunctionService = chatFunctionService;
    }

    @GetMapping("/setDeviceStatusFunction")
    public String setDeviceStatusFunction(@RequestParam String q) {
        return chatFunctionService.setDeviceStatusFunction(q);
    }

    @GetMapping("/setDeviceStatusMethod")
    public String setDeviceStatusMethod(@RequestParam String q) {
        return chatFunctionService.setDeviceStatusMethod(q);
    }


    @GetMapping(value = "/readFile", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public String readFile(@RequestParam String p) {
        return chatFunctionService.readFile(p);
    }


}
