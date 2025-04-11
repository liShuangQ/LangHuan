<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
import { http } from "@/plugins/axios";
import axios, { CancelToken } from "axios";
import { CancelTokenSource } from "axios/index";
import { ElMessage } from "element-plus";
import { chatServiceTypeOption } from "./config"
import aimodel from "@/store/aimodel"
import { documentRankHandleApi } from "@/api/rag";
let chats = ref<Chat[]>([

]);
let chatServiceType = ref<string>('/chat');
let inputMessageText = ref<string>('');
let inputPromptText = ref<string>('');
let currentChatId = ref<string>();
let isTyping = ref<boolean>(false);
let axiosCancel: CancelTokenSource | null = null;
let ragEnabled = ref<boolean>(true)
let toolEnabled = ref<boolean>(false)
let ragGroup = ref<string>('')
let ragGroupOption = ref<{ label: string, value: string }[]>([])
let toolGroup = ref<string>('')
let toolGroupOption = ref<{ label: string, value: string }[]>([])
let aiOptionVisible = ref<boolean>(false)
let chatModelOption = ref<{ label: string, value: string }[]>([])
let chatModelName = ref<string>('')
let aiRagDocumentVisible = ref<boolean>(false)
let aiRagDocumentData = ref<any[]>([])
// 添加滚动到最新的消息
const toDownPage = (): void => {
    nextTick(() => {
        const chatWindow = document.querySelector('.flex-1.overflow-y-auto.p-4.space-y-4');
        if (chatWindow) {
            chatWindow.scrollTop = chatWindow.scrollHeight;
        }
    });
};
// 添加信息 HACK 跟随 interface Message  更改 !!!
const addMessage = (chat: Chat, messageData: Message): void => {
    chat.messages.push({
        id: Date.now(),
        text: messageData.text,
        recommend: messageData?.recommend ?? [],
        rag: messageData?.rag ?? [],
        isUser: messageData.isUser,
        topInfo: getChatTopInfo()
    });
    isTyping.value = messageData.isUser
    toDownPage()
}
// 发送信息
const sendMessage = (recommend = null): void => {
    if (!chatModelName.value) {
        ElMessage.error('请选择模型。')
        return
    }
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
            url: chatServiceType.value + '/chat',
            method: 'post',
            q_spinning: false,
            cancelToken: axiosCancel.token,
            data: {
                id: chat.id,
                p: inputPromptText.value,
                q: inputTextCopy,
                isRag: ragEnabled.value,
                groupId: ragGroup.value,
                isFunction: toolEnabled.value,
                modelName: chatModelName.value
            }
        }).then((res) => {
            if (res.code === 200) {
                try {
                    addMessage(chat,
                        {
                            text: res.data.chat,
                            recommend: res.data.recommend && (JSON.parse(res.data.recommend)?.desc ?? []),
                            rag: res.data?.rag ?? [],
                            isUser: false,
                            topInfo: getChatTopInfo()
                        }
                    )
                } catch (error) {
                    addMessage(chat,
                        {
                            text: res.data.chat,
                            recommend: res.data.recommend,
                            rag: res.data?.rag ?? [],
                            isUser: false,
                            topInfo: getChatTopInfo()
                        }
                    )
                }
            } else {
                addMessage(chat,
                    {
                        text: "回答出现错误，请换种方式提问。",
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
// 优化替换提示词到输入框
const optimizePromptWords = (): void => {
    http.request<any>({
        url: chatServiceType.value + '/getPrompt',
        method: 'post',
        q_spinning: true,
        data: {
            q: inputMessageText.value
        }
    }).then((res) => {
        if (res.code === 200) {
            inputMessageText.value = res.data
        }
    })
}
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
// 清空对话记忆
const clearChatMemory = (isList = false, id = ''): void => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (chat) {
        const useId = isList ? id : chat.id
        http.request<any>({
            url: chatServiceType.value + '/clearChatMemory',
            method: 'post',
            q_spinning: true,
            data: {
                id: useId,
            }
        }).then((res: any) => {
            if (isList) {
                chats.value = chats.value.filter(c => c.id !== useId)
            } else {
                chat.messages.push({
                    id: Date.now(),
                    text: '已成功清空上下文',
                    isUser: false,
                    topInfo: getChatTopInfo()
                });
                toDownPage()
            }
        })
    }

};
// 清空聊天记录
const clearMessage = (): void => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    if (chat) {
        chat.messages = [];
    }
    ElMessage.success('已成功清空聊天记录。');
}
// 添加初始的对话消息
const addStartMessage = (): void => {
    const chat = chats.value.find(c => c.id === currentChatId.value);
    chat && addMessage(chat, {
        text: '很高兴见到你！我可以帮你写代码、读文件、写作各种创意内容，请把你的任务交给我吧~',
        recommend: ['你是谁？', '你能做些什么？'],
        isUser: false,
        topInfo: getChatTopInfo()
    })
}
// 开始一个新的对话
const startNewChat = (): void => {
    const newChatId = 'chat' + Date.now();
    chats.value.push({
        id: newChatId,
        messages: [],
        active: false
    });
    currentChatId.value = newChatId;
    addStartMessage()
};
// 设置对话窗口
const switchChat = (chatId: string): void => {
    currentChatId.value = chatId;
};
// 找到当前的窗口
const currentChat = (): Chat | any => {
    return chats.value.find(c => c.id === currentChatId.value) || { messages: [] };
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

    return `${toRaw(chatModelName.value)}：      ${getCurrentDateTime()}`
};
// rag按钮变化
const ragEnabledChange = (e: any): void => {
    ragGroup.value = ''
}
// 当前选择智能体的改变
const chatServiceTypeChange = (e: any): void => {
    if (e === '/onlyRag') {
        ragEnabled.value = true
    }

}
// 打开引用的文档
const openRagDocumentView = (rag: any[]) => {
    aiRagDocumentData.value = rag
    aiRagDocumentVisible.value = true
}
// 文档的点踩机制
const documentRankHandle = async (t: 'good' | 'bad', d: any) => {
    const res: any = await documentRankHandleApi(d.id, d.metadata.rank, t)
    if (res.code === 200) {
        ElMessage.success(res.data)
    }
}

// 初始化执行
nextTick(async () => {
    await aimodel().setModelOptions()
    chatModelOption.value = toRaw(aimodel().getModelOptions()) as any
    chatModelName.value = chatModelOption.value[0].value
    addStartMessage()
    getRagGroupOptionList()
    startNewChat()

})

</script>

<template>
    <div class="w-full h-full bg-white flex gap-4 relative">

        <!-- 对话窗口 -->
        <div v-if="chats.length > 0" class="flex-1 bg-white rounded-lg shadow-lg flex flex-col h-full">
            <div class="p-4 pb-3 border-b-2  border-gray-300">
                欢迎来到LangHuan
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

                        <div
                            :class="message.isUser ? 'flex items-center justify-end' : 'flex items-center justify-start'">
                            <!-- <div v-html="message.text" :class="[
                                'max-full p-3 rounded-lg transition-all duration-200',
                                message.isUser ? 'bg-blue-500 text-white' : 'bg-gray-200'
                            ]"></div> -->
                            <v-md-preview :text="message.text"></v-md-preview>
                        </div>
                        <!--                        推荐列表-->
                        <div v-if="(message?.recommend ?? []).length > 0"
                            class="w-full flex justify-start items-center mt-1">
                            <div class="bg-blue-100 w-max mr-2 px-1.5 py-0.5 text-[14px] rounded-md cursor-pointer"
                                v-for="item in message.recommend" :key="item" @click="sendMessage(item)">
                                {{ item }}
                            </div>
                        </div>
                        <!--                        引用的知识 -->
                        <div>
                            <el-button v-if="message.rag.length > 0" style="padding-bottom: 0px;" :key="'text'"
                                @click="openRagDocumentView(message.rag)" text>引用的知识库</el-button>
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
                <!-- 上功能区 -->
                <div class="flex items-center justify-between">
                    <!-- 左 -->
                    <div class="flex justify-end items-center">
                        <el-select v-model="chatModelName" :disabled="isTyping" placeholder="选择模型" class="mr-2">
                            <el-option v-for="item in chatModelOption" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                        <el-select v-model="chatServiceType" :disabled="isTyping" placeholder="选择对话类" class="mr-2"
                            @change="chatServiceTypeChange">
                            <el-option v-for="item in chatServiceTypeOption" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                    </div>
                    <!-- 右 -->
                    <div class="flex justify-end items-center">
                        <el-button @click="clearChatMemory(false)" :disabled="isTyping" type="primary">
                            <span class="font-medium text-white">
                                清除记忆
                            </span>
                        </el-button>

                        <el-button @click="clearMessage()" :disabled="isTyping" type="primary">
                            <span class="font-medium text-white">
                                清除聊天记录
                            </span>
                        </el-button>

                    </div>
                </div>


                <!-- 消息内容 -->
                <div class=" flex justify-between items-center ">
                    <div class="text-sm font-medium text-gray-700 mr-2 w-16 ">消息内容</div>
                    <el-input v-model="inputMessageText" type="textarea" autosize placeholder="输入消息内容..."
                        @keyup.enter="sendMessage()"></el-input>
                </div>
                <!-- 下功能区 -->
                <div class=" flex justify-end">
                    <!-- HACK 暂时关闭工具调用！！！ -->
                    <el-switch v-model="toolEnabled" inactive-text="工具" :disabled="true || isTyping"
                        :active-value="true" :inactive-value="false" active-color="#3b82f6" inactive-color="#dc2626"
                        class="mr-2" />
                    <el-select v-if="toolEnabled" v-model="toolGroup" :disabled="isTyping" placeholder="选择工具组"
                        class="mr-2">
                        <el-option v-for="item in toolGroupOption" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                    <el-switch v-model="ragEnabled" inactive-text="RAG" :disabled="isTyping" :active-value="true"
                        :inactive-value="false" active-color="#3b82f6" inactive-color="#dc2626" class="mr-2"
                        @change="ragEnabledChange" />
                    <el-select v-if="ragEnabled" v-model="ragGroup" :disabled="isTyping" placeholder="选择文件组"
                        class="mr-2">
                        <el-option v-for="item in ragGroupOption" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                    <el-button @click="optimizePromptWords()" :disabled="isTyping" type="primary">
                        <span class="font-medium text-white">
                            优化提示词
                        </span>
                    </el-button>
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


        <!-- 设置区域 -->
        <el-dialog v-model="aiOptionVisible" title="设置" width="700">
            <div>
                <div class=" flex justify-between items-center ">
                    <div class="text-sm font-medium text-gray-700 mb-1 w-[60px]">提示词</div>
                    <el-input v-model="inputPromptText" type="textarea" placeholder="Type your prompt..."></el-input>
                </div>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="aiOptionVisible = false">
                        关闭
                    </el-button>
                </div>
            </template>
        </el-dialog>
        <!-- 引用的知识 -->
        <el-dialog v-model="aiRagDocumentVisible" title="引用的知识库" width="900">
            <div class="h-[50vh] overflow-y-scroll">
                <div v-for="(item) in aiRagDocumentData" :key="item.id">
                    <div class="mb-4">
                        {{ item.text }}
                    </div>
                    <div class=" flex justify-start items-center gap-1">
                        <el-tag type="primary">排名：{{ item.metadata.rank }}</el-tag>
                        <el-tag type="primary">距离：{{ item.metadata.distance }}</el-tag>
                        <el-tag type="primary">文件名：{{ item.metadata.filename }}</el-tag>
                        <img class="cursor-pointer" @click="documentRankHandle('good', item)"
                            style="height: 20px;margin: 0 8px;" src="./good.svg" alt="" srcset="">
                        <img class="cursor-pointer" @click="documentRankHandle('bad', item)" style="height: 20px;"
                            src="./bad.svg" alt="" srcset="">
                    </div>
                    <el-divider />
                </div>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="aiRagDocumentVisible = false">
                        关闭
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style>
.github-markdown-body {
    padding: 16px 8px 0 16px !important;
    background-color: aliceblue;
}

.github-markdown-body p {
    margin-bottom: 8px;
}
</style>
