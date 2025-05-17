package com.langhuan.serviceai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.functionTools.RestRequestTools;
import com.langhuan.model.domain.TUserChatWindow;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.service.TPromptsService;
import com.langhuan.service.TUserChatWindowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatService {

    @Autowired
    JdbcChatMemoryRepository chatMemoryRepository;
    @Autowired
    RagService ragService;
    @Autowired
    TUserChatWindowService userChatWindowService;

    ChatClient chatClient;
    ChatMemory chatMemory;

    public ChatService(ChatClient.Builder chatClientBuilder, RagService ragService) {
        this.ragService = ragService;

        chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()

                )
                .build();
    }


    public ChatModelResult chat(String id, String p, String q, Boolean isRag, String groupId, Boolean isFunction, String modelName) {
        ToolCallback[] tools = isFunction ? ToolCallbacks.from(new RestRequestTools()) :
                ToolCallbacks.from();

        try {
            if (isRag) {
                return this.isRagChat(id, p, q, groupId, modelName, tools);
            } else {
                return this.noRagChat(id, p, q, modelName, tools);
            }
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

    public ChatModelResult isRagChat(String id, String p, String q, String groupId, String modelName, ToolCallback[] tools) {
        // 自带方法 不好做排序
//        QuestionAnswerAdvisor questionAnswerAdvisor = groupId.isEmpty()
//                ? new QuestionAnswerAdvisor(VectorStoreConfig,
//                SearchRequest.builder().topK(Constant.WITHTOPK)
//                        .similarityThreshold(Constant.WITHSIMILARITYTHRESHOLD).build(), Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT)
//                : new QuestionAnswerAdvisor(VectorStoreConfig,
//                SearchRequest.builder().topK(c)
//                        .filterExpression("groupId == '" + groupId + "'")//设置过滤条件
//                        .similarityThreshold(Constant.WITHSIMILARITYTHRESHOLD).build(), Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT);


        // 使用排序后的结果手动喂给ai
        List<Document> documentList = ragService.ragSearch(q, groupId, "");
        StringBuilder ragContents = new StringBuilder();
        for (Document document : documentList) {
            ragContents.append(document.getText()).append(";").append("\n");
        }
        String ragPrompt = Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT.replace("{question_answer_context}", ragContents.toString());
        String chat = this.chatClient.prompt(
                        new Prompt(
                                ragPrompt + "\n" + p,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()
                        )
                )
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("ChatService"))
//                .advisors(questionAnswerAdvisor) // 官方的rag方法
                .advisors(a -> a.param(chatMemory.CONVERSATION_ID, id))
                .toolCallbacks(tools)
                .call().content();
        ChatModelResult chatModelResult = new ChatModelResult();
        chatModelResult.setChat(chat);
        chatModelResult.setRag(documentList);
        return chatModelResult;
    }

    public ChatModelResult noRagChat(String id, String p, String q, String modelName, ToolCallback[] tools) {
        String chat = this.chatClient.prompt(
                        new Prompt(
                                p,
                                OpenAiChatOptions.builder()
                                        .model(modelName)
                                        .build()
                        )
                )
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("ChatService"))
                .advisors(a -> a.param(chatMemory.CONVERSATION_ID, id))
                .toolCallbacks(tools)
                .call().content();
        ChatModelResult chatModelResult = new ChatModelResult();
        chatModelResult.setChat(chat);
        chatModelResult.setRag(new ArrayList<>());
        return chatModelResult;
    }


    public List<String> getChatMemoryWindows() {
        log.info("ChatMemory-get-windows");
        List<TUserChatWindow> list = userChatWindowService.list(
                new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getUserId, SecurityContextHolder.getContext().getAuthentication().getName())
        );

        return list.stream().map(TUserChatWindow::getConversationId).toList();
    }


    public List<Message> getChatMemory(String id) {
        log.info("ChatMemory-get: {}", id);
        return chatMemoryRepository.findByConversationId(id);
    }

    public void initChatMemory(String id) {
        log.info("ChatMemory-init: {}", id);
        List<Message> byConversationId = chatMemoryRepository.findByConversationId(id);
        // 不知道为啥 内存中在存入的时候，非得拼用户id。这里拼接用户是为了配合内存中存储默认会带用户的情况，但是实际前端使用的是不带用户的，这样添加后在下面读取的时候可以更好的配合新的内存进行读取
        // 规则就是操作内存对象的时候，需要带用户id。操作数据库直接用对话id即可。
        chatMemory.add(SecurityContextHolder.getContext().getAuthentication().getName() + '_' + id, byConversationId);
    }

    public String saveChatMemory(String id) {
        log.info("ChatMemory-save: {}", id);
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        long count = userChatWindowService.count(new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getConversationId, id));
        if (count == 0) {
            userChatWindowService.save(new TUserChatWindow() {{
                setUserId(user_id);
                setConversationId(id);
            }});
        }
        //  内存中是有 用户_ 的，所以这里拼上，实际存储存储不带用户的
        List<Message> messages = chatMemory.get(user_id + '_' + id);
        chatMemoryRepository.saveAll(id, messages);
        return "保存成功";
    }

    public String clearChatMemory(String id) {
        log.info("ChatMemory-clear: {}", id);
        // 清空内存 HACK 这里可能产生无用内存
        chatMemory.clear(SecurityContextHolder.getContext().getAuthentication().getName() + '_' + id);
        // 删除窗口表
        userChatWindowService.remove(new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getConversationId, id));
        // 清空对话记录
        chatMemoryRepository.deleteByConversationId(id);
        return "清除成功";
    }


}
