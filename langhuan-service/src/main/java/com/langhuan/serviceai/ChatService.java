package com.langhuan.serviceai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.functionTools.RestRequestTools;
import com.langhuan.model.domain.TUserChatWindow;
import com.langhuan.model.pojo.ChatModelResult;
import com.langhuan.model.pojo.ChatRestOption;
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
                .maxMessages(Constant.MESSAGEWINDOWCHATMEMORYMAX)
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()

                )
                .build();
    }

    /**
     * 聊天服务主方法
     * 
     * @param chatRestOption 聊天请求选项对象
     * @return 聊天结果
     */
    public ChatModelResult chat(ChatRestOption chatRestOption) {
        ToolCallback[] tools = chatRestOption.getIsFunction() ? ToolCallbacks.from(new RestRequestTools())
                : ToolCallbacks.from();

        String AINULLDEFAULTUSERPROMPT = TPromptsService
                .getCachedTPromptsByMethodName("AINULLDEFAULTUSERPROMPT");

        if (AINULLDEFAULTUSERPROMPT == null) {
            AINULLDEFAULTUSERPROMPT = Constant.AINULLDEFAULTUSERPROMPT;
        }

        String userPrompt = AINULLDEFAULTUSERPROMPT.replace("{user_prompt}",
                chatRestOption.getQuestion());

        try {
            if (chatRestOption.getIsRag()) {
                return this.isRagChat(chatRestOption.getChatId(), chatRestOption.getPrompt(),
                        userPrompt, chatRestOption.getRagGroupId(),
                        chatRestOption.getModelName(), chatRestOption.getIsReRank(), tools);
            } else {
                return this.noRagChat(chatRestOption.getChatId(), chatRestOption.getPrompt(),
                        userPrompt, chatRestOption.getModelName(), tools);
            }
        } catch (Exception e) {
            log.error("advisor-error: {}", e.getMessage());
            throw new BusinessException("抱歉，我暂时无法回答这个问题。");
        }
    }

    public ChatModelResult isRagChat(String id, String p, String q, String groupId, String modelName, Boolean isReRank,
            ToolCallback[] tools) throws Exception {
        String AIDEFAULTQUESTIONANSWERADVISORRPROMPT = TPromptsService
                .getCachedTPromptsByMethodName("AIDEFAULTQUESTIONANSWERADVISORRPROMPT");
        if (AIDEFAULTQUESTIONANSWERADVISORRPROMPT == null) {
            AIDEFAULTQUESTIONANSWERADVISORRPROMPT = Constant.AIDEFAULTQUESTIONANSWERADVISORRPROMPT;
        }

        // 使用排序后的结果手动喂给ai
        List<Document> documentList = ragService.ragSearch(q, groupId, "", isReRank);
        StringBuilder ragContents = new StringBuilder();
        for (Document document : documentList) {
            ragContents.append(document.getText()).append(";").append("\n");
        }
        String ragPrompt = AIDEFAULTQUESTIONANSWERADVISORRPROMPT
                .replace("{question_answer_context}",
                        ragContents.toString());
        String chat = this.chatClient.prompt(
                new Prompt(
                        ragPrompt + "\n" + p,
                        OpenAiChatOptions.builder()
                                .model(modelName)
                                .build()))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("ChatService"))
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
                                .build()))
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

    public Boolean setChatMemoryWindowsName(String conversationId, String conversationName) {
        log.info("ChatMemory-set-windows-name");
        return userChatWindowService.update(new LambdaUpdateWrapper<TUserChatWindow>()
                .eq(TUserChatWindow::getConversationId, conversationId)
                .set(TUserChatWindow::getConversationName, conversationName));
    }

    public List<TUserChatWindow> getChatMemoryWindows() {
        log.info("ChatMemory-get-windows");
        List<TUserChatWindow> list = userChatWindowService.list(
                new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getUserId,
                        SecurityContextHolder.getContext().getAuthentication().getName()));

        return list;
    }

    public List<Message> getChatMemory(String id) {
        log.info("ChatMemory-get: {}", id);
        return chatMemoryRepository.findByConversationId(id);
    }

    public void initChatMemory(String id) {
        log.info("ChatMemory-init: {}", id);
        List<Message> byConversationId = chatMemoryRepository.findByConversationId(id);
        // 不知道为啥
        // 内存中在存入的时候，非得拼用户id。这里拼接用户是为了配合内存中存储默认会带用户的情况，但是实际前端使用的是不带用户的，这样添加后在下面读取的时候可以更好的配合新的内存进行读取
        // 规则就是操作内存对象的时候，需要带用户id。操作数据库直接用对话id即可。
        chatMemory.add(SecurityContextHolder.getContext().getAuthentication().getName() + '_' + id, byConversationId);
    }

    public String saveChatMemory(String id, String windowName) {
        log.info("ChatMemory-save: {}", id);
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        long count = userChatWindowService
                .count(new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getConversationId, id));
        if (count == 0) {
            userChatWindowService.save(new TUserChatWindow() {
                {
                    setUserId(user_id);
                    setConversationName(windowName);
                    setConversationId(id);
                }
            });
        }
        // 内存中是有 用户_ 的，所以这里拼上，实际存储存储不带用户的
        List<Message> messages = chatMemory.get(user_id + '_' + id);
        chatMemoryRepository.saveAll(id, messages);
        return "保存成功";
    }

    // HACK 当用户不存窗口，不存对话记录的时候，直接关闭页面，会导致内存中有数据，但是表内没有，下次id也不会被匹配上，导致出现无意义内存占用的情况
    // 窗口无感知的保存，这样id不会丢，内存id也可以占用上
    // 想办法从内存中找到所有的id，匹配窗口中是不是有对应id，找时机清空（最好关联用户，比如登陆的时候清空掉内存）（或者简单方式定时清空）。

    public String clearChatMemory(String id) {
        log.info("ChatMemory-clear: {}", id);
        // 清空内存
        chatMemory.clear(SecurityContextHolder.getContext().getAuthentication().getName() + '_' + id);
        // 删除窗口表
        userChatWindowService
                .remove(new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getConversationId, id));
        // 清空对话记录
        chatMemoryRepository.deleteByConversationId(id);
        return "清除成功";
    }

}
