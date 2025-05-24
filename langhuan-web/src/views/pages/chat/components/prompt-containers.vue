<script lang="ts" setup>
import { ref, nextTick, watch } from 'vue'

interface Message {
    id: string;
    content: string;
    sender: 'user' | 'assistant';
    timestamp: string;
}

const props = defineProps<{
    messages: Message[];
    canSend?: boolean;
}>();

const emit = defineEmits<{
    (e: 'send-message', message: string): void;
    (e: 'action', type: string, payload?: any): void;
}>();

const suggestions = [
    { id: 'saveMemory', text: '保存记录' },
    { id: 'optimizePrompt', text: '优化提示词' },
];

const messageInput = ref('');
const messageContainer = ref<HTMLDivElement | null>(null);

const handleSubmit = (e: Event) => {
    e.preventDefault();
    if (!props.canSend || !messageInput.value.trim()) return;

    emit('send-message', messageInput.value);
    messageInput.value = '';
};

const scrollToBottom = () => {
    nextTick(() => {
        if (messageContainer.value) {
            messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
        }
    });
};

// 监听消息变化
watch(() => props.messages, () => {
    scrollToBottom();
}, { deep: true });

// 暴露给父组件的方法
defineExpose({
    clearInput: () => {
        messageInput.value = '';
    }
});
</script>

<template>
    <!-- Prompt Messages Container - Modify the height according to your need -->
    <div class="flex h-[97vh] w-full flex-col">
        <!-- Prompt Messages -->
        <div ref="messageContainer"
            class="flex-1 overflow-y-auto rounded-xl bg-slate-200 p-4 text-sm leading-6 text-slate-900 dark:bg-slate-800 dark:text-slate-300 sm:text-base sm:leading-7">
            <template v-for="msg in messages" :key="msg.id">
                <!-- 消息主体部分 -->
                <div class="flex flex-row px-2 py-4 sm:px-4">
                    <img class="mr-2 flex h-8 w-8 rounded-full sm:mr-4"
                        :src="`https://dummyimage.com/256x256/${msg.sender === 'user' ? '363536' : '354ea1'}/ffffff&text=${msg.sender === 'user' ? 'U' : 'G'}`" />
                    <div :class="[
                        'flex flex-1 items-center rounded-xl',
                        msg.sender === 'assistant' ? 'bg-slate-50 px-2 py-4 dark:bg-slate-900 sm:px-4' : ''
                    ]">
                        <p v-if="msg.sender === 'user'" class="w-full">{{ msg.content }}</p>
                        <v-md-preview v-else :text="msg.content"></v-md-preview>
                    </div>
                </div>
                <!-- 操作按钮部分 -->
                <div class="mb-2 flex w-full flex-row justify-end gap-x-2 text-slate-500">
                    <div v-if="msg.sender === 'assistant'">
                        <button class="hover:text-blue-600">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" stroke-width="2"
                                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path
                                    d="M7 11v8a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1v-7a1 1 0 0 1 1 -1h3a4 4 0 0 0 4 -4v-1a2 2 0 0 1 4 0v5h3a2 2 0 0 1 2 2l-1 5a2 3 0 0 1 -2 2h-7a3 3 0 0 1 -3 -3">
                                </path>
                            </svg>
                        </button>
                        <button class="hover:text-blue-600" type="button">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" stroke-width="2"
                                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path
                                    d="M7 13v-8a1 1 0 0 0 -1 -1h-2a1 1 0 0 0 -1 1v7a1 1 0 0 0 1 1h3a4 4 0 0 1 4 4v1a2 2 0 0 0 4 0v-5h3a2 2 0 0 0 2 -2l-1 -5a2 3 0 0 0 -2 -2h-7a3 3 0 0 0 -3 3">
                                </path>
                            </svg>
                        </button>
                        <button class="hover:text-blue-600" type="button">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" stroke-width="2"
                                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path d="M8 8m0 2a2 2 0 0 1 2 -2h8a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2z">
                                </path>
                                <path d="M16 8v-2a2 2 0 0 0 -2 -2h-8a2 2 0 0 0 -2 2v8a2 2 0 0 0 2 2h2"></path>
                            </svg>
                        </button>
                    </div>

                </div>
            </template>
        </div>
        <!-- Prompt suggestions -->
        <div
            class="mt-4 flex w-full gap-x-2 overflow-x-auto whitespace-nowrap text-xs text-slate-600 dark:text-slate-300 sm:text-sm">
            <button v-for="item in suggestions" :key="item.id" @click="$emit('action', item.id)"
                class="rounded-lg bg-slate-200 p-2 hover:bg-blue-600 hover:text-slate-200 dark:bg-slate-800 dark:hover:bg-blue-600 dark:hover:text-slate-50">
                {{ item.text }}
            </button>


        </div>
        <!-- Prompt message input -->
        <div class="mt-2">
            <label for="chat-input" class="sr-only">Enter your prompt</label>
            <div class="relative">
                <button type="button"
                    class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-500 hover:text-blue-600 dark:text-slate-400 dark:hover:text-blue-600">
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
                <textarea v-model="messageInput" @keydown.enter="handleSubmit" id="chat-input"
                    class="block w-full resize-none rounded-xl border-none bg-slate-200 p-4 pl-10 pr-20 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-slate-800 dark:text-slate-200 dark:placeholder-slate-400 dark:focus:ring-blue-500 sm:text-base"
                    placeholder="Enter your prompt" rows="1" required></textarea>
                <button type="submit" @click="handleSubmit" :disabled="!canSend" :class="[
                    'absolute bottom-2 right-2.5 rounded-lg px-4 py-2 text-sm font-medium text-slate-200 focus:outline-none focus:ring-4 sm:text-base',
                    canSend
                        ? 'bg-blue-700 hover:bg-blue-800 focus:ring-blue-300 dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800'
                        : 'bg-slate-400 cursor-not-allowed'
                ]">
                    Send
                    <span class="sr-only">Send message</span>
                </button>
            </div>
        </div>
    </div>
</template>
