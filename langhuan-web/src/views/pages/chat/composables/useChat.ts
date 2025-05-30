import { ref } from "vue";
import * as api from "../api";
import axios, { CancelTokenSource } from "axios";
import type { Message, ChatMessage } from "../types";
import { ElMessage } from "element-plus";
import { data } from "autoprefixer";
import { documentRankHandleApi } from "@/api/rag";
import { tr } from "element-plus/es/locale";
export function useChat() {
    const messages = ref<Message[]>([]);
    const canSend = ref(true);
    let axiosCancel: CancelTokenSource | null = null;
    let lastMessageContent = "";

    const sendMessage = async (
        windowId: string,
        message: string,
        chatParams = {}
    ) => {
        const assistantMessage = {
            id: "loading-" + Date.now().toString(),
            content: "正在思考中...",
            sender: "assistant" as const,
            timestamp: new Date().toISOString(),
            loading: true,
        };

        const userMessage = {
            id: Date.now().toString(),
            content: message,
            sender: "user" as const,
            timestamp: new Date().toISOString(),
        };

        axiosCancel = axios.CancelToken.source();
        messages.value.push(userMessage, assistantMessage);
        canSend.value = false;

        try {
            const res = await api.sendChatMessage(
                {
                    id: windowId,
                    p: "",
                    q: message,
                    isRag: false,
                    groupId: "",
                    isFunction: false,
                    modelName: "",
                    ...chatParams,
                },
                axiosCancel.token
            );

            lastMessageContent = message;

            // 替换loading消息
            const index = messages.value.findIndex(
                (m) => m.id === assistantMessage.id
            );
            if (index !== -1) {
                messages.value[index] = {
                    id: Date.now().toString(),
                    content: res.data.chat,
                    rag: res.data?.rag ?? [],
                    sender: "assistant",
                    timestamp: new Date().toISOString(),
                };
            }
        } catch (error) {
            if (!axios.isCancel(error)) {
                console.error("Error:", error);
            }
        } finally {
            canSend.value = true;
        }
    };

    const saveMemory = async (windowId: string) => {
        if (!windowId) {
            ElMessage({
                message: "请创建窗口",
                type: "warning",
            });
            return;
        }
        if (lastMessageContent === "") {
            ElMessage({
                message: "请先发送消息",
                type: "warning",
            });
            return;
        }

        const res = await api.saveChatMemory(windowId, lastMessageContent);
        ElMessage({
            message: res.data,
            type: "success",
        });
    };

    const loadMessages = async (windowId: string) => {
        const res = await api.getChatMemory(windowId);
        messages.value = res.data.map((item: any, index: number) => ({
            id: (windowId + index).toString(),
            content: item.text,
            sender:
                item.metadata.messageType === "ASSISTANT"
                    ? "assistant"
                    : "user",
            timestamp: "",
        }));
    };
    const optimizePrompt = async (message: string) => {
        const res = await api.optimizePromptWords(message);
        return res.data;
    };

    const handleMessageAction = async (
        type: string,
        msg: Message & { suggestion?: string }
    ) => {
        if (type === "like" || type === "dislike") {
            if (lastMessageContent === "") {
                ElMessage({
                    message: "请先发送消息",
                    type: "warning",
                });
                return;
            }
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
                })
                .then(() => {
                    ElMessage({
                        message: "感谢您的反馈",
                        type: "success",
                    });
                });
        } else if (type === "copy") {
            navigator.clipboard.writeText(msg.content);
        }
    };
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

    return {
        messages,
        canSend,
        sendMessage,
        saveMemory,
        loadMessages,
        optimizePrompt,
        handleMessageAction,
        documentRank,
    };
}
