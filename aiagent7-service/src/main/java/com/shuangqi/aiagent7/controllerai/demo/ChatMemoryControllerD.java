package com.shuangqi.aiagent7.controllerai.demo;

import com.shuangqi.aiagent7.serviceai.demo.ChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chatMemory")
public class ChatMemoryControllerD {

    private final ChatMemoryService chatMemoryService;

    public ChatMemoryControllerD(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
    }

    /**
     * 带有记忆的聊天
     *
     * @param id
     * @param q
     * @return
     */
    @GetMapping("/chat")
    String chat(@RequestParam String id, @RequestParam String q) {
        return chatMemoryService.chat(id, q);
    }

    /**
     * 带有记忆的聊天-流式返回
     *
     * @param id
     * @param q
     * @return
     */
    @GetMapping("/stream")
    Flux<String> chatStream(@RequestParam String id, @RequestParam String q) {
        return chatMemoryService.chatStream(id, q);
    }

    /**
     * 有提示词带有记忆的聊天
     *
     * @param id
     * @param p
     * @param q
     * @return
     */
    @GetMapping("/chatWithPrompt")
    String chatWithPrompt(@RequestParam String id, @RequestParam String p, @RequestParam String q) {
        return chatMemoryService.chatWithPrompt(id, p, q);
    }

    /**
     * 有提示词带有记忆的聊天-流式返回
     *
     * @param id
     * @param p
     * @param q
     * @return
     */
    @GetMapping("/chatWithPrompt/stream")
    Flux<String> chatWithPromptStream(@RequestParam String id, @RequestParam String p, @RequestParam String q) {
        return chatMemoryService.chatWithPromptStream(id, p, q);
    }

    /**
     * 清除记忆
     *
     * @param id
     * @return
     */
    @GetMapping("/clear")
    String clear(@RequestParam String id) {
        return chatMemoryService.clear(id);
    }
}
