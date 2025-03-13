package com.langhuan.model.pojo;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyChatMemory implements ChatMemory {
    // 主要数据结构
    Map<String, List<Message>> conversationHistory = new ConcurrentHashMap();

    public MyChatMemory() {
    }

    public void add(String conversationId, List<Message> messages) {
        this.conversationHistory.putIfAbsent(conversationId, new ArrayList());
        ((List)this.conversationHistory.get(conversationId)).addAll(messages);
    }

    public List<Message> get(String conversationId, int lastN) {
        List<Message> all = (List)this.conversationHistory.get(conversationId);
        return all != null ? all.stream().skip((long)Math.max(0, all.size() - lastN)).toList() : List.of();
    }

    public void clear(String conversationId) {
        this.conversationHistory.remove(conversationId);
    }

}
