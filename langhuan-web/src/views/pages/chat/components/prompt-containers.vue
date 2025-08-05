<script lang="ts" setup>
import { ref, nextTick, watch, computed } from "vue";
import FeedbackDialog from "./feedback-dialog.vue";
import RagDocumentDialog from "./rag-document-dialog.vue";
import type { Message } from "../types";

const props = defineProps<{
    messages: Message[];
    canSend?: boolean;
    hasWindows?: boolean; // 添加新的props
}>();

const emit = defineEmits<{
    (e: "send-message", message: string): void;
    (e: "action", type: string, payload?: any): void;
}>();

// 使用Map来缓存已处理的消息内容，避免重复创建ref
const messagePartsCache = new Map();

// 处理消息内容，分离思考过程和正常内容
const processMessageContent = (content: string, messageId: string) => {
    // 如果已经处理过这条消息，直接返回缓存的结果
    if (messagePartsCache.has(messageId)) {
        return messagePartsCache.get(messageId);
    }

    const thinkRegex = /<think>([\s\S]*?)<\/think>/g;
    const parts = [];
    let lastIndex = 0;
    let match;

    while ((match = thinkRegex.exec(content)) !== null) {
        // 添加思考过程之前的内容
        if (match.index > lastIndex) {
            parts.push({
                type: "normal",
                content: content.slice(lastIndex, match.index),
            });
        }

        // 添加思考过程 - 使用ref使其响应式
        parts.push({
            type: "thinking",
            content: match[1].trim(),
            isOpen: ref(false), // 使用ref包装使其响应式
        });

        lastIndex = match.index + match[0].length;
    }

    // 添加剩余内容
    if (lastIndex < content.length) {
        parts.push({
            type: "normal",
            content: content.slice(lastIndex),
        });
    }

    const result = parts.length > 0 ? parts : [{ type: "normal", content }];
    // 缓存结果
    messagePartsCache.set(messageId, result);
    return result;
};

const suggestions = [
    { id: "saveMemory", text: "保存记录" },
    { id: "optimizePrompt", text: "优化提示词" },
];

const messageInput = ref("");
const messageContainer = ref<HTMLDivElement | null>(null);
const getMessageInput = () => messageInput.value;
const setMessageInput = (value: string) => {
    messageInput.value = value;
};
const handleSubmit = (e: Event) => {
    e.preventDefault();
    if (!props.canSend || !messageInput.value.trim()) return;

    emit("send-message", messageInput.value);
    messageInput.value = "";
};

const scrollToBottom = () => {
    nextTick(() => {
        if (messageContainer.value) {
            messageContainer.value.scrollTop =
                messageContainer.value.scrollHeight;
        }
    });
};

// 监听消息变化
watch(
    () => props.messages,
    () => {
        console.log(props.messages, "props.messages");
        scrollToBottom();
    },
    { deep: true }
);

// 暴露给父组件的方法
defineExpose({
    getMessageInput,
    setMessageInput,
    clearInput: () => {
        messageInput.value = "";
    },
});

const showFeedbackDialog = ref(false);
const currentFeedback = ref<{
    type: "like" | "dislike";
    message: Message;
} | null>(null);

const handleFeedback = (type: "like" | "dislike", msg: Message) => {
    currentFeedback.value = { type, message: msg };
    showFeedbackDialog.value = true;
};

const handleFeedbackConfirm = (suggestion: string) => {
    if (currentFeedback.value) {
        emit("action", currentFeedback.value.type, {
            ...currentFeedback.value.message,
            suggestion,
        });
    }
};

const ragDocumentVisible = ref(false);
const ragDocuments = ref<any[]>([]);

const handleRagChecks = (msg: Message) => {
    ragDocuments.value = msg.rag || [];
    ragDocumentVisible.value = true;
};
const userImage = require("../imgs/user.png");
const assistantImage = require("../imgs/assistant.png");

const handleRagRank = (type: "good" | "bad", document: any) => {
    emit("action", "documentRank", { type, document });
};
</script>

<template>
    <div class="flex h-full w-full flex-col">
        <!-- 添加空状态显示 -->
        <div
            v-if="!hasWindows"
            class="flex-1 flex flex-col items-center justify-center text-slate-500"
        >
            <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-12 w-12 mb-4"
                viewBox="0 0 24 24"
                stroke-width="1"
                stroke="currentColor"
                fill="none"
            >
                <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                <path d="M8 9h8" />
                <path d="M8 13h6" />
                <path
                    d="M12.01 18.594l-4.01 2.406v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v5.5"
                />
                <path d="M16 19h6" />
                <path d="M19 16v6" />
            </svg>
            <p class="text-lg font-medium mb-2">开始新的对话</p>
            <p class="text-sm">
                点击左上角的"新建对话"按钮创建一个新的对话窗口
            </p>
        </div>

        <!-- 原有的消息列表容器 -->
        <div
            v-else
            ref="messageContainer"
            class="flex-1 overflow-y-auto rounded-xl bg-slate-200 p-4 text-sm leading-6 text-slate-900 dark:bg-slate-800 dark:text-slate-300 sm:text-base sm:leading-7"
        >
            <!-- 欢迎提示框 - 当有窗口但没有消息时显示 -->
            <div
                v-if="messages.length === 0"
                class="flex flex-col items-center justify-center h-full text-slate-500"
            >
                <div
                    class="bg-white dark:bg-slate-700 rounded-2xl p-8 shadow-lg max-w-md mx-auto text-center"
                >
                    <h3
                        class="text-xl font-semibold text-slate-800 dark:text-slate-200 mb-3"
                    >
                        欢迎使用
                    </h3>
                    <p
                        class="text-slate-600 dark:text-slate-400 mb-6 leading-relaxed"
                    >
                        我是您的AI助手，可以帮助您解答问题、处理任务和进行创意讨论。
                        请在下方输入您的问题或需求，让我们开始对话吧！
                    </p>
                    <div class="flex flex-wrap gap-2 justify-center">
                        <span
                            class="px-3 py-1 bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 rounded-full text-sm"
                        >
                            💡 创意写作
                        </span>
                        <span
                            class="px-3 py-1 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 rounded-full text-sm"
                        >
                            📚 知识问答
                        </span>
                    </div>
                </div>
            </div>

            <template v-for="(msg, index) in messages" :key="msg.id">
                <!-- 消息主体部分-->
                <div
                    class="flex flex-row px-2 py-4 sm:px-4"
                    v-if="msg.sender === 'user' ? (
                        msg?.showUserMessage !== undefined
                                ? msg.showUserMessage
                                : true
                    ) : true"
                >
                    <img
                        class="mr-2 flex h-8 w-8 rounded-full sm:mr-4"
                        :src="`${
                            msg.sender === 'user' ? userImage : assistantImage
                        }`"
                    />
                    <div class="flex flex-col">
                        <div class="text-sm font-medium text-slate-600 mb-1">
                            {{
                                msg.sender === "user"
                                    ? "用户"
                                    : msg.chatSettings?.fileGroupName ?? ""
                            }}
                        </div>
                        <div
                            :class="[
                                'flex flex-1 items-center rounded-xl',
                                msg.sender === 'assistant'
                                    ? 'bg-slate-50 px-2 py-4 dark:bg-slate-900 sm:px-4'
                                    : '',
                            ]"
                        >
                            <p v-if="msg.sender === 'user'" class="w-full">
                                {{ msg.content }}
                            </p>
                            <template v-else>
                                <div
                                    v-if="msg.loading"
                                    class="flex items-center space-x-2"
                                >
                                    <span>{{ msg.content }}</span>
                                    <span
                                        class="loading loading-dots loading-sm"
                                    ></span>
                                </div>
                                <div v-else class="w-full">
                                    <template
                                        v-for="(
                                            part, partIndex
                                        ) in processMessageContent(
                                            msg.content,
                                            msg.id
                                        )"
                                        :key="partIndex"
                                    >
                                        <!-- 正常内容 -->
                                        <div
                                            v-if="part.type === 'normal'"
                                            class="mb-2"
                                        >
                                            <v-md-preview
                                                :text="part.content"
                                            ></v-md-preview>
                                        </div>
                                        <!-- 思考过程 -->
                                        <div
                                            v-else-if="part.type === 'thinking'"
                                            class="mb-4 border border-gray-200 rounded-md"
                                        >
                                            <div
                                                class="flex items-center p-1 bg-gray-100 cursor-pointer"
                                                @click="
                                                    part.isOpen.value =
                                                        !part.isOpen.value
                                                "
                                            >
                                                <span class="mr-2">💭</span>
                                                <span
                                                    class="font-normal text-sm"
                                                    >思考过程</span
                                                >
                                                <span class="ml-auto">
                                                    {{
                                                        part.isOpen.value
                                                            ? "▼"
                                                            : "▶"
                                                    }}&nbsp;
                                                </span>
                                            </div>
                                            <div
                                                v-show="part.isOpen.value"
                                                class="p-3 bg-gray-50"
                                            >
                                                <pre
                                                    class="whitespace-pre-wrap text-sm"
                                                    >{{ part.content }}</pre
                                                >
                                            </div>
                                        </div>
                                    </template>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>
                <!-- 操作按钮部分 -->
                <div class="mb-2 w-full">
                    <div
                        v-if="
                            index === messages.length - 1 &&
                            msg.sender === 'assistant'
                        "
                        class="flex flex-row justify-end gap-x-2 text-slate-500"
                    >
                        <button
                            @click="handleFeedback('like', msg)"
                            class="hover:text-blue-600"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                class="h-5 w-5"
                                viewBox="0 0 24 24"
                                stroke-width="2"
                                stroke="currentColor"
                                fill="none"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                            >
                                <path
                                    stroke="none"
                                    d="M0 0h24v24H0z"
                                    fill="none"
                                ></path>
                                <path
                                    d="M7 11v8a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1v-7a1 1 0 0 1 1 -1h3a4 4 0 0 0 4 -4v-1a2 2 0 0 1 4 0v5h3a2 2 0 0 1 2 2l-1 5a2 3 0 0 1 -2 2h-7a3 3 0 0 1 -3 -3"
                                ></path>
                            </svg>
                        </button>
                        <button
                            @click="handleFeedback('dislike', msg)"
                            class="hover:text-blue-600"
                            type="button"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                class="h-5 w-5"
                                viewBox="0 0 24 24"
                                stroke-width="2"
                                stroke="currentColor"
                                fill="none"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                            >
                                <path
                                    stroke="none"
                                    d="M0 0h24v24H0z"
                                    fill="none"
                                ></path>
                                <path
                                    d="M7 13v-8a1 1 0 0 0 -1 -1h-2a1 1 0 0 0 -1 1v7a1 1 0 0 0 1 1h3a4 4 0 0 1 4 4v1a2 2 0 0 0 4 0v-5h3a2 2 0 0 0 2 -2l-1 -5a2 3 0 0 0 -2 -2h-7a3 3 0 0 0 -3 3"
                                ></path>
                            </svg>
                        </button>
                        <button
                            @click="$emit('action', 'copy', msg)"
                            class="hover:text-blue-600"
                            type="button"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                class="h-5 w-5"
                                viewBox="0 0 24 24"
                                stroke-width="2"
                                stroke="currentColor"
                                fill="none"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                            >
                                <path
                                    stroke="none"
                                    d="M0 0h24v24H0z"
                                    fill="none"
                                ></path>
                                <path
                                    d="M8 8m0 2a2 2 0 0 1 2 -2h8a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2z"
                                ></path>
                                <path
                                    d="M16 8v-2a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v8a2 2 0 0 0 2 2h2"
                                ></path>
                            </svg>
                        </button>
                        <button
                            v-if="(msg.rag ?? []).length > 0"
                            @click="handleRagChecks(msg)"
                            class="hover:text-blue-600"
                            type="button"
                        >
                            <svg
                                t="1748096953676"
                                class="h-5 w-5"
                                viewBox="0 0 1024 1024"
                                version="1.1"
                                xmlns="http://www.w3.org/2000/svg"
                                p-id="5234"
                                width="200"
                                height="200"
                            >
                                <path
                                    d="M912.9 129.3H769.2c-24.9 0-45 20.1-45 45v677.8c0 24.9 20.1 45 45 45h143.7c24.9 0 45-20.1 45-45V174.3c0-24.8-20.1-45-45-45z m-27 72v466.9h-89.7V201.3h89.7z m-89.7 623.8v-84.9h89.7v84.9h-89.7zM636.8 129.3H493.1c-24.9 0-45 20.1-45 45v677.8c0 24.9 20.1 45 45 45h143.7c24.9 0 45-20.1 45-45V174.3c0-24.8-20.2-45-45-45z m-27 72v466.9h-89.7V201.3h89.7z m-89.7 623.8v-84.9h89.7v84.9h-89.7zM409.3 162.7l-140-32.5c-3.4-0.8-6.8-1.2-10.2-1.2-20.5 0-39 14.1-43.8 34.8L65.6 808.9c-5.6 24.2 9.5 48.4 33.7 54l140 32.5c3.4 0.8 6.8 1.2 10.2 1.2 20.5 0 39-14.1 43.8-34.8l116-499.9c0.3-1 0.6-2.1 0.9-3.2 0.2-1.1 0.4-2.1 0.6-3.2L443 216.6c5.6-24.1-9.5-48.3-33.7-53.9z m-130 43.7l87.4 20.3-18.7 80.6-87.4-20.3 18.7-80.6z m-50 612.8l-87.4-20.3 102.5-441.7 87.4 20.3-102.5 441.7z"
                                    p-id="5235"
                                    fill="#515151"
                                ></path>
                                <!-- #707070 -->
                            </svg>
                        </button>
                    </div>
                </div>
            </template>
        </div>

        <!-- 底部输入区域,当有窗口时才显示 -->
        <div v-if="hasWindows" class="mt-4 flex-shrink-0">
            <!-- Prompt suggestions -->
            <div
                class="flex w-full gap-x-2 overflow-x-auto whitespace-nowrap text-xs text-slate-600 dark:text-slate-300 sm:text-sm"
            >
                <button
                    v-for="item in suggestions"
                    :key="item.id"
                    @click="$emit('action', item.id)"
                    class="rounded-lg bg-slate-200 p-2 hover:bg-blue-600 hover:text-slate-200 dark:bg-slate-800 dark:hover:bg-blue-600 dark:hover:text-slate-50"
                >
                    {{ item.text }}
                </button>
            </div>
            <!-- Prompt message input -->
            <div class="mt-2">
                <label for="chat-input" class="sr-only"
                    >Enter your prompt</label
                >
                <div class="relative">
                    <button
                        type="button"
                        class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-500 hover:text-blue-600 dark:text-slate-400 dark:hover:text-blue-600"
                    >
                        <!-- <svg
          aria-hidden="true"
          class="h-5 w-5"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
          stroke-width="2"
          stroke="currentColor"
          fill="none"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
          <path
            d="M9 2m0 3a3 3 0 0 1 3 -3h0a3 3 0 0 1 3 3v5a3 3 0 0 1 -3 3h0a3 3 0 0 1 -3 -3z"
          ></path>
          <path d="M5 10a7 7 0 0 0 14 0"></path>
          <path d="M8 21l8 0"></path>
          <path d="M12 17l0 4"></path>
        </svg> -->
                        <span class="sr-only">Use voice input</span>
                    </button>
                    <textarea
                        v-model="messageInput"
                        @keydown.enter="handleSubmit"
                        id="chat-input"
                        class="block w-full resize-none rounded-xl border-none bg-slate-200 p-4 pl-10 pr-20 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-800 dark:text-slate-200 dark:placeholder-slate-400 dark:focus:ring-blue-500 sm:text-base"
                        placeholder="Enter your message"
                        rows="1"
                        required
                    ></textarea>
                    <button
                        type="submit"
                        @click="handleSubmit"
                        :disabled="!canSend"
                        :class="[
                            'absolute bottom-2 right-2.5 rounded-lg px-4 py-2 text-sm font-medium text-slate-200 focus:outline-none focus:ring-4 sm:text-base',
                            canSend
                                ? 'bg-blue-700 hover:bg-blue-800 focus:ring-blue-300 dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800'
                                : 'bg-slate-400 cursor-not-allowed',
                        ]"
                    >
                        Send
                        <span class="sr-only">Send message</span>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <FeedbackDialog
        v-model="showFeedbackDialog"
        :type="currentFeedback?.type || 'like'"
        @confirm="handleFeedbackConfirm"
    />
    <!-- 添加RAG文档对话框 -->
    <RagDocumentDialog
        v-model="ragDocumentVisible"
        :documents="ragDocuments"
        @rank="handleRagRank"
    />
</template>
