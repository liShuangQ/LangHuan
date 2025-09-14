package com.langhuan.serviceai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.langhuan.common.Constant;
import com.langhuan.model.domain.TUserChatWindow;
import com.langhuan.service.TUserChatWindowService;
import com.langhuan.utils.chatMemory.MyJdbcChatMemoryRepository;
import com.langhuan.utils.chatMemory.MyMessageWindowChatMemory;
import com.langhuan.utils.other.SecurityUtils;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * 聊天记忆管理服务类
 *
 * 负责聊天窗口管理、历史记录管理和持久化存储
 *
 * @author lishuangqi
 * @since 2025-01-01
 */
@Service
@Slf4j
public class ChatMemoryService {

    private final TUserChatWindowService userChatWindowService;
    @Autowired
    private MyJdbcChatMemoryRepository myJdbcChatMemoryRepository;
    private MyMessageWindowChatMemory chatMemory;

    public ChatMemoryService(TUserChatWindowService userChatWindowService) {
        this.userChatWindowService = userChatWindowService;
    }

    @PostConstruct
    public void init() {
        this.chatMemory = new MyMessageWindowChatMemory(myJdbcChatMemoryRepository, Constant.MESSAGEWINDOWCHATMEMORYMAX);
    }

    /**
     * 获取聊天记忆管理器
     *
     * @return 聊天记忆管理器实例
     */
    public ChatMemory getChatMemory() {
        return this.chatMemory;
    }

    /**
     * 设置聊天窗口名称
     *
     * @param conversationId   会话ID
     * @param conversationName 新的会话名称
     * @return 更新是否成功
     */
    public Boolean setChatMemoryWindowsName(String conversationId, String conversationName) {
        log.info("ChatMemory-set-windows-name");
        return userChatWindowService.update(new LambdaUpdateWrapper<TUserChatWindow>()
                .eq(TUserChatWindow::getConversationId, conversationId)
                .set(TUserChatWindow::getConversationName, conversationName));
    }

    /**
     * 获取当前用户的所有聊天窗口
     *
     * @return 用户的聊天窗口列表
     */
    public List<TUserChatWindow> getChatMemoryWindows() {
        log.info("ChatMemory-get-windows");
        // 获取当前登录用户ID
        String currentUserId = SecurityUtils.getCurrentUsername();

        // 查询该用户的所有聊天窗口
        return userChatWindowService.list(
                new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getUserId, currentUserId));
    }

    /**
     * 获取指定会话的聊天历史
     *
     * @param id 会话ID
     * @return 聊天消息列表
     */
    public List<Map<String, Object>> getChatMemoryMessages(String id) {
        log.info("ChatMemory-get-messages: {}", id);
        // 提供的管理方式
        return chatMemory.myGet(id);
    }

    /**
     * 保存聊天记录到窗口表，回复新的ID
     */
    public String saveChatMemory(String id, String windowName) {
        log.info("ChatMemory-save: {}", id);
        String user_id = SecurityUtils.getCurrentUsername();
        // 如果是新窗口 则创建新的会话ID，否则则肯定是从数据库中查询出来的会话ID
        if (id.equals("new")) {
            String uuid = IdUtil.randomUUID();
            userChatWindowService.save(new TUserChatWindow() {
                {
                    setUserId(user_id);
                    setConversationName(windowName);
                    // 会话ID = 用户名 + 会话ID。因为springai的记忆在存储的时候会这样。
                    setConversationId(uuid);
                }
            });
            return uuid;
        }
        return id;
    }

    /**
     * 清除指定会话的所有数据
     *
     * @param id 会话ID
     * @return 清除结果提示信息
     */
    public String clearChatMemory(String id) {
        log.info("ChatMemory-clear: {}", id);
        // 清除聊天记忆
        chatMemory.clear(id);
        // 删除用户聊天窗口记录
        userChatWindowService
                .remove(new LambdaQueryWrapper<TUserChatWindow>().eq(TUserChatWindow::getConversationId, id));

        return "清除成功";
    }
}
