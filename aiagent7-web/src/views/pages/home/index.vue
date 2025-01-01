<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
import {http} from "@/plugins/axios";

const chats = ref<Chat[]>([
    {
        id: 1,
        messages: [],
        active: true
    }
]);

const inputMessageText = ref<string>('');
const inputPromptText = ref<string>('');
const currentChatId = ref<number>(1);
const isTyping = ref<boolean>(false);

// 添加滚动到最新的消息
const toDownPage = () => {
    nextTick(() => {
        const chatWindow = document.querySelector('.flex-1.overflow-y-auto.p-4.space-y-4');
        if (chatWindow) {
            chatWindow.scrollTop = chatWindow.scrollHeight;
        }
    });
};
// 发送信息
const sendMessage = (recommend = null) => {
    if (recommend) {
        inputMessageText.value = recommend;
    }
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (inputMessageText.value.trim() && chat) {
        chat.messages.push({
            id: Date.now(),
            text: inputMessageText.value,
            isUser: true
        });
        toDownPage()
        const inputTextCopy = inputMessageText.value;
        inputMessageText.value = '';
        isTyping.value = true;
        http.request<any>({
            url: '/chat/chat',
            method: 'get',
            q_spinning: false,
            params: {
                id: chat.id,
                p: inputPromptText.value,
                q: inputTextCopy
            }
        }).then((res: any) => {
            // HACK 跟随 interface Message  更改
            chat.messages.push({
                id: Date.now(),
                text: res.desc,
                recommend: res.recommend,
                isUser: false
            });
            isTyping.value = false;
            toDownPage()
        });

        toDownPage()
    }
};
// 清空对话记忆
const clearMemory = (isList = false, id = 0) => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (chat) {
        const useId = isList ? id : chat.id
        http.request<any>({
            url: '/chat/clear',
            method: 'get',
            q_spinning: true,
            params: {
                id: useId,
            }
        }).then((res: any) => {
            if (isList) {
                chats.value = chats.value.filter(c => c.id !== useId)
            } else {
                chat.messages.push({
                    id: Date.now(),
                    text: '已成功清空上下文',
                    isUser: false
                });
                toDownPage()
            }
        })
    }

};
// 清空聊天记录
const clearMessage = () => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (chat) {
        chat.messages = [];
    }
    ElMessage.success('已成功清空聊天记录');
}
// 开始一个新的对话
const startNewChat = () => {
    const newChatId = (chats.value[chats.value.length - 1]?.id ?? 0) + 1;
    chats.value.push({
        id: newChatId,
        messages: [],
        active: false
    });
    currentChatId.value = newChatId;
};
// 设置对话窗口
const switchChat = (chatId: number) => {
    currentChatId.value = chatId;
};
// 找到当前的窗口
const currentChat = (): Chat | any => {
    return chats.value.find(c => c.id === currentChatId.value) || {messages: []};
};
// 获取当前时间
const getCurrentTime = () => {
    return Date.now();
};
</script>

<template>
    <div class="w-full h-full bg-white flex gap-4 relative">

        <!-- 对话窗口列表 -->
        <div
            :class="[
              'w-64 bg-white rounded-lg shadow-lg p-4 transition-transform duration-300',
              'fixed md:static h-full] z-40 overflow-y-auto',
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
            <ul class="space-y-2 ">
                <li
                    v-for="chat in chats"
                    :key="chat.id"
                    :class="[
                  'p-2 rounded-lg transition-colors duration-200 flex justify-between items-center',
                  chat.id === currentChatId ? 'bg-blue-100' : 'hover:bg-gray-100'
                ]"
                >
                    <span class="cursor-pointer" @click="switchChat(chat.id)">
                        Chat #{{ chat.id }}
                    </span>
                    <span class="text-blue-500 cursor-pointer" @click="clearMemory(true,chat.id)">清除</span>
                </li>
            </ul>
        </div>

        <!-- 对话窗口 -->
        <div v-if="chats.length > 0" class="flex-1 bg-white rounded-lg shadow-lg flex flex-col h-full">
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
                    <div class="max-w-[80%]">
                        <!--                        对话回复-->
                        <div class="text-[12px] " v-format-time>{{ getCurrentTime() }}</div>
                        <div
                            :class="[
                    'max-full p-3 rounded-lg transition-all duration-200',
                    message.isUser ? 'bg-blue-500 text-white' : 'bg-gray-200'
                  ]"
                        >
                            {{ message.text }}
                        </div>
                        <!--                        推荐列表-->
                        <div v-if="(message?.recommend??[]).length > 0"
                             class="w-full flex justify-start items-center mt-1">
                            <div class="bg-blue-100 w-max mr-2 px-1.5 py-0.5 text-[14px] rounded-md cursor-pointer"
                                 v-for="item in message.recommend" :key="item"
                                 @click="sendMessage(item)"
                            >
                                {{ item }}
                            </div>
                        </div>
                    </div>
                    <div v-if="message.isUser"
                         class="w-8 h-8 rounded-full bg-gray-500 flex items-center justify-center text-white">
                        Me
                    </div>
                </div>

                <!-- ai的等待效果 -->
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

            <!-- 功能区域 -->
            <div class="p-4 border-t border-gray-200 space-y-3">
                <div class="flex justify-end">
                    <el-button
                        @click="clearMemory(false)"
                        :class="['!bg-blue-500 hover:!bg-green-600']"
                    >
                  <span class="font-medium text-white">
                    清除记忆
                  </span>
                    </el-button>

                    <el-button
                        @click="clearMessage()"
                        :class="['!bg-blue-500 hover:!bg-green-600']"
                    >
                  <span class="font-medium text-white">
                    清除聊天记录
                  </span>
                    </el-button>
                </div>

                <!--                提示词-->
                <el-input
                    v-model="inputPromptText"
                    placeholder="Type your prompt..."
                    class="w-full"
                ></el-input>
                <!--                用户消息-->
                <el-input
                    v-model="inputMessageText"
                    placeholder="Type your message..."
                    @keyup.enter="sendMessage()"
                    class="w-full"
                >
                    <template #append>
                        <el-button
                            @click="sendMessage()"
                            class="!px-6 !py-3 !bg-blue-500 !text-white hover:!bg-blue-600 active:!bg-blue-700 transition-colors duration-200 flex items-center justify-center gap-2"
                            style="height: 100%"
                        >
                            <span class="font-medium">Send</span>
                            <el-icon class="text-lg">
                            </el-icon>
                        </el-button>
                    </template>
                </el-input>
            </div>
        </div>
        <div v-else class="flex-1 bg-white rounded-lg shadow-lg flex flex-col h-full items-center justify-center">
            请开启新的对话
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
