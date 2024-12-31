<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
import {ref, computed, onMounted} from 'vue';
import {http} from "@/plugins/axios";
import dayjs from "dayjs";
import {paginationConfig, tableData} from "@/views/pages/systemManagement/user/tableConfig";

interface Message {
    id: number;
    text: string;
    isUser: boolean;
}

interface Chat {
    id: number;
    messages: Message[];
    active: boolean;
}

const chats = ref<Chat[]>([
    {
        id: 1,
        messages: [],
        active: true
    }
]);

const inputText = ref<string>('');
const currentChatId = ref<number>(1);
const ragEnabled = ref<boolean>(false);
const isTyping = ref<boolean>(false);

// Load RAG state from localStorage
onMounted(() => {
    const savedState = localStorage.getItem('ragEnabled');
    if (savedState !== null) {
        ragEnabled.value = savedState === 'true';
    }
});


const sendMessage = () => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (inputText.value.trim() && chat) {
        chat.messages.push({
            id: Date.now(),
            text: inputText.value,
            isUser: true
        });
        const inputTextCopy = inputText.value;
        inputText.value = '';
        isTyping.value = true;
        http.request<any>({
            url: '/chatMemory/chat',
            method: 'get',
            q_spinning: false,
            params: {
                id: chat.id,
                q: inputTextCopy
            }
        }).then((res: any) => {
            chat.messages.push({
                id: Date.now(),
                text: res.desc,
                isUser: false
            });
            isTyping.value = false;
        })
    }
};
const clearMemory = () => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (chat) {
        http.request<any>({
            url: '/chatMemory/clear',
            method: 'get',
            q_spinning: false,
            params: {
                id: chat.id,
            }
        }).then((res: any) => {
            ElMessage.success(res)
        })
    }

};
const startNewChat = () => {
    const newChatId = chats.value.length + 1;
    chats.value.push({
        id: newChatId,
        messages: [],
        active: false
    });
    currentChatId.value = newChatId;
};

const switchChat = (chatId: number) => {
    currentChatId.value = chatId;
};

const toggleRAG = () => {
    ragEnabled.value = !ragEnabled.value;
    localStorage.setItem('ragEnabled', ragEnabled.value.toString());
};


const currentChat = (): Chat | any => {
    return chats.value.find(c => c.id === currentChatId.value) || {messages: []};
};
</script>

<template>
    <div class="min-h-screen h-full bg-gray-100 p-4 flex items-center justify-center">
        <div class="w-full h-full max-w-6xl mx-4 mt-6 flex gap-4 relative">

            <!-- Chat List -->
            <div
                :class="[
              'w-64 bg-white rounded-lg shadow-lg p-4 transition-transform duration-300',
              'fixed md:static h-[calc(100%-8rem)] z-40 overflow-y-auto',
            ]"
            >
                <div class="flex justify-between items-center mb-4">
                    <h2 class="text-lg font-semibold">Chats</h2>
                    <el-button
                        @click="startNewChat"
                        class="!px-3 !py-2 !bg-blue-500 !text-white hover:!bg-blue-600 active:!bg-blue-700 transition-colors duration-200"
                    >
                        New Chat
                    </el-button>
                </div>
                <ul class="space-y-2">
                    <li
                        v-for="chat in chats"
                        :key="chat.id"
                        @click="switchChat(chat.id)"
                        :class="[
                  'p-2 rounded-lg cursor-pointer transition-colors duration-200',
                  chat.id === currentChatId ? 'bg-blue-100' : 'hover:bg-gray-100'
                ]"
                    >
                        Chat #{{ chat.id }}
                    </li>
                </ul>
            </div>

            <!-- Chat Window -->
            <div class="flex-1 bg-white rounded-lg shadow-lg flex flex-col h-[calc(100%-8rem)]">
                <div class="flex-1 overflow-y-auto p-4 space-y-4">
                    <div
                        v-for="message in currentChat().messages"
                        :key="message.id"
                        :class="['flex items-start gap-3', message.isUser ? 'justify-end' : 'justify-start']"
                    >
                        <div v-if="!message.isUser"
                             class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white">
                            AI
                        </div>
                        <div
                            :class="[
                    'max-w-[80%] p-3 rounded-lg transition-all duration-200',
                    message.isUser ? 'bg-blue-500 text-white' : 'bg-gray-200'
                  ]"
                        >
                            {{ message.text }}
                        </div>
                        <div v-if="message.isUser"
                             class="w-8 h-8 rounded-full bg-gray-500 flex items-center justify-center text-white">
                            Me
                        </div>
                    </div>

                    <!-- Typing Indicator -->
                    <div v-if="isTyping" class="flex items-start gap-3">
                        <div class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white">
                            AI
                        </div>
                        <div class="max-w-[80%] p-3 rounded-lg bg-gray-200">
                            <div class="flex space-x-1">
                                <div class="w-2 h-2 bg-gray-500 rounded-full animate-bounce"></div>
                                <div class="w-2 h-2 bg-gray-500 rounded-full animate-bounce delay-100"></div>
                                <div class="w-2 h-2 bg-gray-500 rounded-full animate-bounce delay-200"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Input and RAG Toggle -->
                <div class="p-4 border-t border-gray-200 space-y-3">
                    <div class="flex justify-end">
                        <el-button
                            @click="toggleRAG"
                            :class="[
                    '!px-4 !py-2 transition-colors duration-200',
                    ragEnabled ? '!bg-green-500 hover:!bg-green-600' : '!bg-gray-300 hover:!bg-gray-400'
                  ]"
                        >
                  <span class="font-medium text-white">
                    {{ ragEnabled ? '禁用RAG' : '启用RAG' }}
                  </span>
                        </el-button>
                        <el-button
                            @click="clearMemory"
                            :class="['!bg-blue-500 hover:!bg-green-600']"
                        >
                  <span class="font-medium text-white">
                    清除记忆
                  </span>
                        </el-button>
                    </div>
                    <el-input
                        v-model="inputText"
                        placeholder="Type your message..."
                        @keyup.enter="sendMessage"
                        class="w-full"
                    >
                        <template #append>
                            <el-button
                                @click="sendMessage"
                                class="!px-6 !py-3 !bg-blue-500 !text-white hover:!bg-blue-600 active:!bg-blue-700 transition-colors duration-200 flex items-center justify-center gap-2"
                                style="height: 100%"
                            >
                                <span class="font-medium">Send</span>
                                <el-icon class="text-lg">
                                    <i-ep-send/>
                                </el-icon>
                            </el-button>
                        </template>
                    </el-input>
                </div>
            </div>
        </div>
    </div>
</template>

<style>
.el-input__wrapper {
    border-radius: 0.5rem !important;
}

.el-button {
    border-radius: 0.5rem !important;
}
</style>
