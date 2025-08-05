import { http } from "@/plugins/axios";
import { CancelToken } from "axios";

/**
 * 发送聊天消息
 * @param params 聊天参数
 * @param cancelToken 用于取消请求的token
 */
export const sendChatMessage = (
    params: {
        id: string;
        p: string;
        q: string;
        isRag: boolean;
        isReRank: boolean;
        groupId: string;
        isFunction: boolean;
        modelName: string;
    },
    cancelToken: CancelToken
) => {
    return http.request<any>({
        // url: params.chatServiceType + '/chat',
        url: "chat/chat",
        method: "post",
        q_spinning: false,
        cancelToken,
        data: params,
    });
};

/**
 * 优化提示词
 * @param text 需要优化的文本
 */
export const optimizePromptWords = (text: string) => {
    if (!text) {
        ElMessage.info("请输入需要优化的文本");
        return;
    }
    return http.request<any>({
        url: "/chat/getPrompt",
        method: "post",
        q_spinning: true,
        data: {
            q: text,
        },
    });
};

/**
 * 获取RAG文件组列表
 */
export const getRagGroupOptionList = () => {
    return http.request<any>({
        url: "/rag/file-group/getEnum",
        method: "post",
        q_spinning: true,
        data: {
            isRead: true,
        },
    });
};

/**
 * 保存聊天记忆
 * @param chatId 聊天ID
 * @param name 聊天名称
 */
export const saveChatMemory = (chatId: string, name: string) => {
    return http.request<any>({
        url: "/chat/saveChatMemory",
        method: "post",
        q_spinning: true,
        data: {
            id: chatId,
            name: name,
        },
    });
};

/**
 * 清除聊天记忆
 * @param chatId 聊天ID
 */
export const clearChatMemory = (chatId: string) => {
    return http.request<any>({
        url: "/chat/clearChatMemory",
        method: "post",
        q_spinning: true,
        data: {
            id: chatId,
        },
    });
};

/**
 * 获取所有聊天窗口列表
 */
export const getChatMemoryWindows = () => {
    return http.request<any>({
        url: "/chat/getChatMemoryWindows",
        method: "post",
        q_spinning: true,
    });
};

/**
 * 设置聊天窗口列表名称
 * @param id 聊天窗口id
 * @param name 聊天窗口名称
 */
export const setChatMemoryWindowsName = (id: string, name: string) => {
    return http.request<any>({
        url: "/chat/setChatMemoryWindowsName",
        method: "post",
        q_spinning: true,
        data: {
            id,
            name,
        },
    });
};

/**
 * 获取特定聊天窗口的历史记录
 * @param chatId 聊天ID
 */
export const getChatMemory = (chatId: string) => {
    return http.request<any>({
        url: "/chat/getChatMemory",
        method: "post",
        q_spinning: true,
        data: {
            id: chatId,
        },
    });
};

/**
 * 提交用户反馈
 * @param params 反馈参数
 */
export const submitFeedback = (params: {
    questionId: string;
    questionContent: string;
    answerContent: string;
    interaction: string;
    knowledgeBaseIds: string;
    suggestion: string;
}) => {
    return http.request<any>({
        url: "/chatFeedback/add",
        method: "post",
        q_spinning: true,
        q_contentType: "json",
        data: params,
    });
};
