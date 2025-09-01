package com.langhuan.serviceai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.langhuan.common.Constant;
import com.langhuan.config.MyJdbcChatMemoryRepository;
import com.langhuan.config.MyPostgresChatMemoryRepositoryDialect;
import com.langhuan.model.domain.TUserChatWindow;
import com.langhuan.service.TUserChatWindowService;
import com.langhuan.utils.other.SecurityUtils;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final MyPostgresChatMemoryRepositoryDialect myPostgresChatMemoryRepositoryDialect;
    private final JdbcTemplate jdbcTemplate;
    private ChatMemory chatMemory;

    public ChatMemoryService(TUserChatWindowService userChatWindowService,
            MyPostgresChatMemoryRepositoryDialect myPostgresChatMemoryRepositoryDialect, JdbcTemplate jdbcTemplate) {
        this.userChatWindowService = userChatWindowService;
        this.myPostgresChatMemoryRepositoryDialect = myPostgresChatMemoryRepositoryDialect;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 设置聊天记忆仓库
     * 
     * @param repository 聊天记忆仓库
     */
    @Autowired
    public void setChatMemoryRepository() {
        // 创建聊天记忆仓库
        ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(myPostgresChatMemoryRepositoryDialect)
                .build();
        // 构建ChatMemory以包含仓库
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(Constant.MESSAGEWINDOWCHATMEMORYMAX)
                .build();
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
     * 见 JdbcChatMemoryRepository 提供的MessageRowMapper。
     * 自定义回复参数，这时候已经改变了返回格式，注意类型为SYSTEM和TOOL的类型需要特殊处理（当前因为没需求，数据库不存储这种类型，所以不处理）
     */
    private static class MessageRowMapper implements RowMapper<Map<String, Object>> {

        @Override
        @Nullable
        public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {

            var content = rs.getString(1);
            // var type = MessageType.valueOf(rs.getString(2));
            var time = rs.getString(3);
            return Map.of(
                    "messageType", rs.getString(2),
                    "media", Array.newInstance(Object.class, 0),
                    "text", content,
                    "time", time // 将时间属性添加到结果中
            );
            // return switch (type) {
            // case USER -> new UserMessage(content);
            // case ASSISTANT -> new AssistantMessage(content);
            // case SYSTEM -> new SystemMessage(content);
            // // The content is always stored empty for ToolResponseMessages.
            // // If we want to capture the actual content, we need to extend
            // // AddBatchPreparedStatement to support it.
            // case TOOL -> new ToolResponseMessage(List.of());
            // };
        }

    }

    /**
     * 获取指定会话的聊天历史
     *
     * @param id 会话ID
     * @return 聊天消息列表
     */
    public List<Map<String, Object>> getChatMemoryMessages(String id) {
        log.info("ChatMemory-get-messages: {}", id);
        // 为了添加时间呈现改为直接在sql中获取，跳过chatMemory提供的封装方法，原理一致
        return this.jdbcTemplate.query(
                this.myPostgresChatMemoryRepositoryDialect.getSelectMessagesSql(),
                new MessageRowMapper(), id);
        // 提供的管理方式
        // return chatMemory.get(id);
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
            String conversationId = user_id + "_" + uuid;
            userChatWindowService.save(new TUserChatWindow() {
                {
                    setUserId(user_id);
                    setConversationName(windowName);
                    // 会话ID = 用户名 + 会话ID。因为springai的记忆在存储的时候会这样。
                    setConversationId(conversationId);
                }
            });
            return conversationId;
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