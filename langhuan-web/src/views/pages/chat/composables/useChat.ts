import { ref } from "vue";
import * as api from "../api";
import axios, { CancelTokenSource } from "axios";
import type { Message, ChatMessage } from "../types";
import { ElMessage } from "element-plus";

export function useChat() {
    const messages = ref<Message[]>([]);
    const canSend = ref(true);
    let axiosCancel: CancelTokenSource | null = null;
    let lastMessageContent = "";

    const sendMessage = async (windowId: string, message: string) => {
        const newMessage = {
            id: Date.now().toString(),
            content: message,
            sender: "user" as const,
            timestamp: new Date().toISOString(),
        };

        axiosCancel = axios.CancelToken.source();
        messages.value.push(newMessage);
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
                    modelName: "qwen2.5:0.5b",
                },
                axiosCancel.token
            );

            lastMessageContent = message;

            messages.value.push({
                id: Date.now().toString(),
                content: res.data.chat,
                rag: res.data?.rag ?? [],
                sender: "assistant",
                timestamp: new Date().toISOString(),
            });
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

    const handleMessageAction = async (type: string, msg: Message) => {
        if (type === 'like' || type === 'dislike') {
            console.log(msg,'msgmsgmsg');

            // TODO rag等
            await api.submitFeedback({
                questionId: msg.id,
                questionContent: lastMessageContent,
                answerContent: msg.content,
                interaction: type,
                knowledgeBaseIds: '',
                suggestion: ''
            }).then(() => {
                ElMessage({
                    message: '感谢您的反馈',
                    type: 'success',
                });
            });
        } else if (type === 'copy') {
            navigator.clipboard.writeText(msg.content);
        }
    };

    return {
        messages,
        canSend,
        sendMessage,
        saveMemory,
        loadMessages,
        optimizePrompt,
        handleMessageAction
    };
}
