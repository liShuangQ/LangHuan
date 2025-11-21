/**
 * 聊天功能组合式函数
 * 提供聊天消息发送、加载、优化等核心功能
 */
import { ref } from "vue";
import * as api from "../api";
import dayjs from "dayjs";
import axios, { CancelTokenSource } from "axios";
import type {
    Message,
    ChatFeedback,
    ChatSettings,
    ChatSendParam,
    ChatOption,
} from "../types";
import { ElMessage } from "element-plus";
import { documentRankHandleApi } from "@/api/rag";
import { blobToBase64 } from "@/utils/imgFile";
// 获取项目中文名称
const BASE_PROJECT_NAME_CN = computed(() => {
    return process.env.BASE_PROJECT_NAME_CN as string;
});

/**
 * 去除markdown代码块标记的函数
 * @param content 原始内容
 * @returns 去除代码块标记后的内容
 */
function removeMarkdownCodeBlocks(content: string): string {
    if (!content) return content;

    // 去除开头结尾的空格
    const trimmedContent = content.trim();

    // 检查是否以```markdown开头并以```结尾
    if (
        trimmedContent.startsWith("```markdown") &&
        trimmedContent.endsWith("```")
    ) {
        // 去除开头的```markdown和结尾的```
        return trimmedContent.slice(11, -3).trim();
    }

    // 检查是否以```开头并以```结尾（通用代码块）
    if (trimmedContent.startsWith("```") && trimmedContent.endsWith("```")) {
        // 找到第一个换行符的位置
        const firstNewlineIndex = trimmedContent.indexOf("\n");
        if (firstNewlineIndex !== -1) {
            // 去除开头的```语言标识和结尾的```
            return trimmedContent.slice(firstNewlineIndex + 1, -3).trim();
        } else {
            // 如果没有换行符，直接去除开头和结尾的```
            return trimmedContent.slice(3, -3).trim();
        }
    }

    // 如果没有代码块标记，返回原内容
    return trimmedContent;
}

export function useChat() {
    // 消息列表和发送状态
    const messages = ref<Message[]>([]);
    const canSend = ref(true);
    // 请求取消令牌和最后一条消息内容缓存
    let axiosCancel: CancelTokenSource | null = null;
    let lastMessageContent = "";

    /**
     * 发送聊天消息
     * @param windowId 聊天窗口ID
     * @param chatParams 聊天参数，包含用户消息、附件等信息
     */
    const sendMessage = async (
        windowId: string,
        chatParams: ChatOption = {} //其他信息
    ) => {
        let imgInfo = "";

        // 处理附件文件
        if (chatParams.accessory) {
            const files = chatParams.accessory as Blob[];
            for (let index = 0; index < files.length; index++) {
                const file: Blob = files[index];
                if (file.type.indexOf("image") > -1) {
                    // 处理图片附件，转换为base64格式
                    imgInfo += `![img](${await blobToBase64(file)}) \n`;
                } else {
                    // 处理其他类型文件
                    imgInfo += `**${file.name}** \n`;
                }
            }
        }

        // 构建用户消息对象
        const userMessageInfo = {
            id: Date.now().toString(),
            content: imgInfo + chatParams.userMessage,
            sender: "user" as const,
            timestamp: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            showUserMessage: chatParams.showUserMessage,
        };

        // 构建助手消息对象（初始显示"正在思考中..."）
        const assistantMessageInfo = {
            id: "loading-" + Date.now().toString(),
            content: "正在思考中...",
            sender: "assistant" as const,
            timestamp: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            loading: true,
        };

        // 添加消息到消息列表
        messages.value.push(userMessageInfo, assistantMessageInfo);

        // 创建取消令牌，禁用发送按钮
        axiosCancel = axios.CancelToken.source();
        canSend.value = false;

        // 获取附件数据并从参数中移除
        const accessory = chatParams?.accessory??[];
        if (chatParams.accessory) {
            delete chatParams.accessory;
        }

        // 构建发送消息的参数
        let sendChatMessageParam = {
            chatId: windowId,
            prompt: "",
            userMessage: chatParams.userMessage,
            isRag: false,
            isReRank: false,
            ragGroupId: "",
            isFunction: false,
            modelName: "",
            ...chatParams,
        } as ChatSendParam;

        try {
            // 发送聊天请求
            const res = await api.sendChatMessage(
                {
                    option: sendChatMessageParam,
                    accessory: accessory,
                },
                axiosCancel!.token
            );

            // 缓存最后一条用户消息内容
            lastMessageContent = chatParams?.userMessage ?? "";

            // 查找并替换loading消息
            const index = messages.value.findIndex(
                (m) => m.id === assistantMessageInfo.id
            );
            if (index !== -1) {
                messages.value[index] = {
                    id: Date.now().toString(),
                    content: removeMarkdownCodeBlocks(res.data.chat),
                    rag: res.data?.rag ?? [],
                    sender: "assistant",
                    timestamp: dayjs().format("YYYY-MM-DD HH:mm:ss"),
                    chatSettings: chatParams,
                };
            }
        } catch (error) {
            if (!axios.isCancel(error)) {
                console.error("Error:", error);
            }
        } finally {
            // 恢复发送状态
            canSend.value = true;
        }
    };

    /**
     * 加载指定聊天窗口的消息历史
     * @param windowId 聊天窗口ID
     */
    const loadMessages = async (windowId: string) => {
        const res = await api.getChatMemoryMessages(windowId);
        // 将后端数据转换为前端消息格式
        messages.value = res.data.map((item: any, index: number) => ({
            id: (windowId + index).toString(),
            content: item.text,
            chatSettings: {
                fileGroupName: BASE_PROJECT_NAME_CN.value,
            },
            sender: item.messageType === "ASSISTANT" ? "assistant" : "user",
            timestamp: item.time ? item.time.split(".")[0] : "",
        }));
    };

    /**
     * 优化用户输入的提示词
     * @param message 原始消息
     * @returns 优化后的提示词
     */
    const optimizePrompt = async (message: string) => {
        const res: any = await api.optimizePromptWords(message);
        return res.data;
    };

    /**
     * 处理消息操作（点赞、点踩、复制等）
     * @param type 操作类型
     * @param msg 消息对象
     * @param settings 聊天设置
     */
    const handleMessageAction = async (
        type: string,
        msg: Message & { suggestion?: string },
        settings: ChatSettings
    ) => {
        if (type === "like" || type === "dislike") {
            // 检查是否有发送过消息
            if (lastMessageContent === "") {
                ElMessage({
                    message: "请先发送消息",
                    type: "warning",
                });
                return;
            }

            // 提交用户反馈
            await api
                .submitFeedback({
                    questionId: msg.id,
                    questionContent: lastMessageContent,
                    answerContent: msg.content,
                    interaction: type,
                    knowledgeBaseIds: (
                        msg.rag?.map((item) => item.id) || []
                    ).join(","),
                    suggestion: msg.suggestion || "",
                    usePrompt: settings.promptTemplate || "",
                    useModel: settings.modelName || "",
                    useRank: settings.isReRank || false,
                    useFileGroupId: settings.ragGroup?.id || "",
                    useFileGroupName: settings.ragGroup?.name || "",
                } as ChatFeedback)
                .then(() => {
                    ElMessage({
                        message: "感谢您的反馈",
                        type: "success",
                    });
                });
        } else if (type === "copy") {
            // 复制消息内容到剪贴板
            navigator.clipboard.writeText(msg.content);
        }
    };

    /**
     * 处理文档相关性评分
     * @param data 包含评分类型和文档信息的数据
     */
    const documentRank = async (data: any) => {
        const { type, document } = data;
        const res: any = await documentRankHandleApi(
            document.id,
            document.metadata.rank,
            type
        );
        if (res.code === 200) {
            // 显示成功消息
            ElMessage.success(res.data);
        }
    };

    // 返回需要对外暴露的属性和方法
    return {
        messages,
        canSend,
        sendMessage,
        loadMessages,
        optimizePrompt,
        handleMessageAction,
        documentRank,
    };
}
