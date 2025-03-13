package com.langhuan.controllerai.demo;

import com.langhuan.serviceai.demo.ChatClientService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatClient")
public class ChatClientControllerD {

    private final ChatClientService chatClientService;

    public ChatClientControllerD(ChatClientService chatClientService) {
        this.chatClientService = chatClientService;
    }

    @GetMapping("/string")
    public String string(@RequestParam String q) {
        return chatClientService.chat(q);
    }
    @GetMapping("/string1")
    public String string1(@RequestParam String q) {
        return chatClientService.chat1(q);
    }
    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam String q) {
        return chatClientService.stream(q);
    }

    @GetMapping("/chatWithPrompt/ChatResponse")
    public ChatResponse chatWithPrompt(@RequestParam String p, @RequestParam String q) {
        return chatClientService.chatWithPrompt(p, q);
    }

    @GetMapping("/chatWithPromptArray/ActorFilms")
    public ChatClientService.ActorFilms chatWithPromptArray(@RequestParam String p, @RequestParam String q) {
        return chatClientService.chatWithPromptArray(p, q);
    }

    @GetMapping("/chatWithPrompt/ListActorFilms")
    public List<ChatClientService.ActorFilms> chatWithPromptListActorFilms(@RequestParam String p, @RequestParam String q) {
        return chatClientService.chatWithPromptListActorFilms(p, q);
    }

    @GetMapping("/chatWithPrompt/Map")
    public Map<String, String> chatWithPromptMap(@RequestParam String p, @RequestParam String q) {
        return chatClientService.chatWithPromptMap(p, q);
    }
}
