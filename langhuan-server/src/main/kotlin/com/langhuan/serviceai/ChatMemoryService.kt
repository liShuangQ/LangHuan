package com.langhuan.serviceai

import cn.hutool.core.util.IdUtil
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.langhuan.common.Constant
import com.langhuan.model.domain.TUserChatWindow
import com.langhuan.service.MinioService
import com.langhuan.service.TUserChatWindowService
import com.langhuan.utils.chatMemory.MyJdbcChatMemoryRepository
import com.langhuan.utils.chatMemory.MyMessageWindowChatMemory
import com.langhuan.utils.other.SecurityUtils
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * 聊天记忆管理服务类
 * <p>
 * 负责聊天窗口管理、历史记录管理和持久化存储
 *
 * @author lishuangqi
 * @since 2025-01-01
 */
@Service
class ChatMemoryService {

    companion object {
        private val log = LoggerFactory.getLogger(ChatMemoryService::class.java)
    }

    private val userChatWindowService: TUserChatWindowService

    @Autowired
    private lateinit var myJdbcChatMemoryRepository: MyJdbcChatMemoryRepository

    @Autowired
    private lateinit var minioService: MinioService

    @Value("\${minio.img-bucket-name}")
    private lateinit var bucket: String

    @Value("\${minio.folder.chat-memory-img}")
    private lateinit var chatMemoryImgFold: String

    private lateinit var chatMemory: MyMessageWindowChatMemory

    constructor(userChatWindowService: TUserChatWindowService) {
        this.userChatWindowService = userChatWindowService
    }

    @PostConstruct
    @Throws(Exception::class)
    fun init() {
        this.chatMemory = MyMessageWindowChatMemory(myJdbcChatMemoryRepository, Constant.MESSAGEWINDOWCHATMEMORYMAX)
        // 在初始化方法中调用 ensureBucketExists
        if (minioService != null) {
            minioService.ensureBucketExists(bucket)
        }
    }

    /**
     * 获取聊天记忆管理器
     *
     * @return 聊天记忆管理器实例
     */
    fun getChatMemory(): ChatMemory {
        return this.chatMemory
    }

    /**
     * 设置聊天窗口名称
     *
     * @param conversationId   会话ID
     * @param conversationName 新的会话名称
     * @return 更新是否成功
     */
    fun setChatMemoryWindowsName(conversationId: String, conversationName: String): Boolean {
        log.info("ChatMemory-set-windows-name")
        return userChatWindowService.update(
            UpdateWrapper<TUserChatWindow>()
                .eq("conversationId", conversationId)
                .set("conversationName", conversationName)
        )
    }

    /**
     * 获取当前用户的所有聊天窗口
     *
     * @return 用户的聊天窗口列表
     */
    fun getChatMemoryWindows(): List<TUserChatWindow> {
        log.info("ChatMemory-get-windows")
        // 获取当前登录用户ID
        val currentUserId = SecurityUtils.getCurrentUsername()

        // 查询该用户的所有聊天窗口
        return userChatWindowService.list(
            QueryWrapper<TUserChatWindow>().eq("user_id", currentUserId)
        )
    }

    /**
     * 获取指定会话的聊天历史
     *
     * @param id 会话ID
     * @return 聊天消息列表
     */
    fun getChatMemoryMessages(id: String): List<Map<String, Any>> {
        log.info("ChatMemory-get-messages: {}", id)
        // 提供的管理方式
        return chatMemory.myGet(id)
    }

    /**
     * 保存聊天记录到窗口表，回复新的ID
     */
    fun saveChatMemory(id: String, windowName: String): String {
        log.info("ChatMemory-save: {}", id)
        val user_id = SecurityUtils.getCurrentUsername()
        // 如果是新窗口 则创建新的会话ID，否则则肯定是从数据库中查询出来的会话ID
        if (id == "new") {
            val uuid = IdUtil.randomUUID()
            val tUserChatWindow: TUserChatWindow = TUserChatWindow()
            tUserChatWindow.conversationId = uuid
            tUserChatWindow.userId = user_id
            tUserChatWindow.conversationName = windowName
            userChatWindowService.save(tUserChatWindow)
            return uuid
        }
        return id
    }

    /**
     * 清除指定会话的所有数据
     *
     * @param id 会话ID
     * @return 清除结果提示信息
     */
    @Throws(Exception::class)
    fun clearChatMemory(id: String): String {
        log.info("ChatMemory-clear: {}", id)
        // 清除聊天记忆
        chatMemory.clear(id)
        // 删除用户聊天窗口记录
        userChatWindowService.remove(
            QueryWrapper<TUserChatWindow>().eq("conversationId", id)
        )
        // 删除记忆中的图片
        minioService.deleteFolder("$chatMemoryImgFold/$id", bucket)
        return "清除成功"
    }
}
