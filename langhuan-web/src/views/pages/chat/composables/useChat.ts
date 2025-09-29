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
const BASE_PROJECT_NAME_CN = computed(() => {
    return process.env.BASE_PROJECT_NAME_CN as string;
});
// 去除markdown代码块标记的函数
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
    const messages = ref<Message[]>([]);
    const canSend = ref(true);
    let axiosCancel: CancelTokenSource | null = null;
    let lastMessageContent = "";

    const sendMessage = async (
        windowId: string,
        chatParams: ChatOption = {} //其他信息
    ) => {
        let imgInfo = "";

        if (chatParams.accessory) {
            const files = chatParams.accessory as Blob[];
            for (let index = 0; index < files.length; index++) {
                const file: Blob = files[index];
                if (file.type.indexOf("image") > -1) {
                    imgInfo += `![img](${await blobToBase64(file)}) \n`;
                }else{
                    imgInfo += `**${file.name}** \n`;
                }
            }
        }

        const userMessageInfo = {
            id: Date.now().toString(),
            content: imgInfo + chatParams.userMessage,
            sender: "user" as const,
            timestamp: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            showUserMessage: chatParams.showUserMessage,
        };

        const assistantMessageInfo = {
            id: "loading-" + Date.now().toString(),
            content: "正在思考中...",
            sender: "assistant" as const,
            timestamp: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            loading: true,
        };
        messages.value.push(userMessageInfo, assistantMessageInfo);
        axiosCancel = axios.CancelToken.source();
        canSend.value = false;

        const accessory = chatParams.accessory;
        delete chatParams.accessory;
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
            const res = await api.sendChatMessage(
                {
                    option: sendChatMessageParam,
                    accessory: accessory,
                },
                axiosCancel!.token
            );

            lastMessageContent = chatParams?.userMessage ?? "";

            // 替换loading消息
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
            canSend.value = true;
        }
    };

    const loadMessages = async (windowId: string) => {
        const res = await api.getChatMemoryMessages(windowId);
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
    const optimizePrompt = async (message: string) => {
        const res: any = await api.optimizePromptWords(message);
        return res.data;
    };

    const handleMessageAction = async (
        type: string,
        msg: Message & { suggestion?: string },
        settings: ChatSettings
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
        loadMessages,
        optimizePrompt,
        handleMessageAction,
        documentRank,
    };
}
