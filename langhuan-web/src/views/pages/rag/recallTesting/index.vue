<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
import {http} from "@/plugins/axios";
import axios, {CancelToken} from "axios";
import {CancelTokenSource} from "axios/index";
import {ElMessage} from "element-plus";

let chats = ref<Chat[]>([
    {
        id: 1,
        messages: [],
        active: true
    }
]);
let inputMessageText = ref<string>('');
let currentChatId = ref<number>(1);
let isTyping = ref<boolean>(false);
let axiosCancel: CancelTokenSource | null = null;
let ragGroup = ref<string>('')
let ragGroupOption = ref<{ label: string, value: string }[]>([])
// 添加滚动到最新的消息
const toDownPage = (): void => {
    nextTick(() => {
        const chatWindow = document.querySelector('.flex-1.overflow-y-auto.p-4.space-y-4');
        if (chatWindow) {
            chatWindow.scrollTop = chatWindow.scrollHeight;
        }
    });
};
// 添加信息 HACK 跟随 interface Message  更改
const addMessage = (chat: Chat, messageData: Message): void => {
    chat.messages.push({
        id: Date.now(),
        text: messageData.text,
        recommend: messageData?.recommend ?? [],
        isUser: messageData.isUser,
        topInfo: getChatTopInfo()
    });

    isTyping.value = messageData.isUser
    toDownPage()
}
// 发送信息
const sendMessage = (recommend = null): void => {
    if (isTyping.value) {
        ElMessage.error('请等待回复完成。')
        return
    }
    if (recommend) {
        inputMessageText.value = recommend;
    }
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (inputMessageText.value.trim() && chat) {
        addMessage(chat, {
            text: inputMessageText.value,
            isUser: true,
            topInfo: getChatTopInfo()
        })
        const inputTextCopy = inputMessageText.value;
        inputMessageText.value = '';
        axiosCancel = axios.CancelToken.source();
        http.request<any>({
            url: '/rag/recallTesting',
            method: 'post',
            q_spinning: false,
            q_contentType: "form",
            cancelToken: axiosCancel.token,
            data: {
                q: inputTextCopy,
                groupId: ragGroup.value,
            }
        }).then((res) => {
            if (res.code === 200) {
                addMessage(chat,
                    {
                        text: res.data && (JSON.parse(res.data)?.desc ?? "json格式错误"),
                        recommend: [],
                        isUser: false,
                        topInfo: getChatTopInfo()
                    }
                )
            } else {
                addMessage(chat,
                    {
                        text: "出现错误。",
                        recommend: [],
                        isUser: false,
                        topInfo: getChatTopInfo()
                    }
                )
            }
        }).catch(error => {
            if (error.code === "ERR_CANCELED") {
                addMessage(chat,
                    {
                        text: "请求已取消。",
                        recommend: [],
                        isUser: false,
                        topInfo: getChatTopInfo()
                    }
                )
            } else {
                console.warn(error, 'error');
                addMessage(chat,
                    {
                        text: "未知错误。",
                        recommend: [],
                        isUser: false,
                        topInfo: getChatTopInfo()
                    }
                )
            }
        })

    }
};
// 获取Rag文件组
const getRagGroupOptionList = (): Promise<any> => {
    return http.request<any>({
        url: '/rag/file-group/getEnum',
        method: 'post',
        q_spinning: true,
        data: {},
    }).then((res) => {
        if (res.code === 200) {
            ragGroupOption.value = res.data.map((e: any) => {
                return {
                    label: e.groupName,
                    value: e.id
                }
            })
            ragGroup.value = ragGroupOption.value[0].value
        }
    })
}
// 停止当前对话
const messageStop = (): void => {
    if (axiosCancel) {
        axiosCancel.cancel();
    }
}
// 添加初始的对话消息
const addStartMessage = (): void => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    chat && addMessage(chat, {
        text: '你好，请开始召回测试。',
        recommend: [],
        isUser: false,
        topInfo: getChatTopInfo()
    })
}

// 找到当前的窗口
const currentChat = (): Chat | any => {
    return chats.value.find(c => c.id === currentChatId.value) || {messages: []};
};
// 对话框上面的信息
const getChatTopInfo = (): string => {
    const getCurrentDateTime = (): string => {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    };

    return `${getCurrentDateTime()}`
};
// 初始化执行
nextTick(async () => {
    addStartMessage()
    getRagGroupOptionList()

})

</script>

<template>
    <div class="w-full h-full bg-white flex gap-4 relative">
        <!-- 对话窗口 -->
        <div v-if="chats.length > 0" class="flex-1 bg-white rounded-lg shadow-lg flex flex-col h-full">
            <div class="p-4 pb-3 border-b-2  border-gray-300">
                召回测试
            </div>

            <div class="flex-1 overflow-y-auto p-4 space-y-4">
                <div v-for="message in currentChat().messages" :key="message.id"
                     :class="['flex items-start gap-3', message.isUser ? 'justify-end' : 'justify-start']">
                    <div v-if="!message.isUser"
                         class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white">
                        AI
                    </div>
                    <div class="max-w-[80%]">
                        <!--                        对话回复-->
                        <div class="text-[12px] ">{{ message.topInfo }}</div>

                        <div v-html="message.text" :class="[
                            'max-full p-3 rounded-lg transition-all duration-200',
                            message.isUser ? 'bg-blue-500 text-white' : 'bg-gray-200'
                        ]"></div>
                        <!--                        推荐列表-->
                        <div v-if="(message?.recommend ?? []).length > 0"
                             class="w-full flex justify-start items-center mt-1">
                            <div class="bg-blue-100 w-max mr-2 px-1.5 py-0.5 text-[14px] rounded-md cursor-pointer"
                                 v-for="item in message.recommend" :key="item" @click="sendMessage(item)">
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
                <div v-show="isTyping" class="flex items-start gap-3">
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

            <div class="p-4 border-t border-gray-200 space-y-3">
                <!-- 消息内容 -->
                <div class=" flex justify-between items-center ">
                    <div class="text-sm font-medium text-gray-700 mr-2 w-16 ">消息内容</div>
                    <el-input v-model="inputMessageText" type="textarea" autosize placeholder="输入消息内容..."
                              @keyup.enter="sendMessage()"></el-input>
                </div>
                <!-- 下功能区 -->
                <div class=" flex justify-end">
                    <div class=" flex justify-between items-center ">
                        <div class="text-sm font-medium text-gray-700 mr-2 w-40 ">选择文件组</div>
                        <el-select v-model="ragGroup" :disabled="isTyping" placeholder="选择文件组" class="mr-2">
                            <el-option v-for="item in ragGroupOption" :key="item.value" :label="item.label"
                                       :value="item.value"/>
                        </el-select>
                    </div>
                    <el-button v-show="!isTyping" @click="sendMessage()" type="primary">
                        <span class="font-medium text-white">
                            发送
                        </span>
                    </el-button>
                    <el-button v-show="isTyping" @click="messageStop()" type="primary">
                        <span class="font-medium text-white">
                            停止
                        </span>
                    </el-button>
                </div>
            </div>
        </div>
        <div v-else class="flex-1 bg-white rounded-lg shadow-lg flex flex-col h-full items-center justify-center">
            请开启新的对话
        </div>

    </div>
</template>

