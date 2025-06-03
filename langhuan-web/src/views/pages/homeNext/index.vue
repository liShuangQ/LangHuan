<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
// 导入必要的依赖和模块
import { http } from "@/plugins/axios";              // 导入HTTP请求工具
import axios, { CancelToken } from "axios";          // 导入Axios及其取消令牌功能
import { CancelTokenSource } from "axios/index";      // 导入取消令牌源类型
import { ElMessage } from "element-plus";            // 导入Element Plus的消息提示组件
import { chatServiceTypeOption } from "./config"     // 导入聊天服务类型选项配置
import aimodel from "@/store/aimodel"                // 导入AI模型状态管理
import { documentRankHandleApi } from "@/api/rag";   // 导入文档评分处理API
import { useRouter } from 'vue-router';              // 导入Vue路由器
import { generateUUID } from '@/utils/uuid'                // 导入UUID生成工具
import user from "@/store/user";
const userStore = user()
// 初始化路由和状态变量
const router = useRouter();
const nowIsChat = router.currentRoute.value.path.includes('chat')
// 聊天窗口列表
let chats = ref<ChatWindow[]>([]);
// 聊天服务类型，默认为'/chat'
let chatServiceType = ref<string>('/chat');
// 输入消息文本
let inputMessageText = ref<string>('');
// 输入提示文本
let inputPromptText = ref<string>('');
// 当前聊天ID
let currentChatId = ref<string>();
// 是否正在输入中
let isTyping = ref<boolean>(false);
// Axios取消令牌，用于取消请求
let axiosCancel: CancelTokenSource | null = null;
// 是否启用RAG(检索增强生成)功能
let ragEnabled = ref<boolean>(true)
// 是否启用工具功能
let toolEnabled = ref<boolean>(false)
// 当前选择的RAG组
let ragGroup = ref<string>('')
// RAG组选项列表
let ragGroupOption = ref<{ label: string, value: string }[]>([])
// 当前选择的工具组
let toolGroup = ref<string>('')
// 工具组选项列表
let toolGroupOption = ref<{ label: string, value: string }[]>([])
// AI选项对话框是否可见
let aiOptionVisible = ref<boolean>(false)
// 聊天模型选项列表
let chatModelOption = ref<{ label: string, value: string }[]>([])
// 当前选择的聊天模型名称
let chatModelName = ref<string>('')
// RAG文档对话框是否可见
let aiRagDocumentVisible = ref<boolean>(false)
// RAG文档数据
let aiRagDocumentData = ref<any[]>([])
// 基础项目名称
const BASE_PROJECT_NAME = computed(() => {
    return process.env.BASE_PROJECT_NAME as string
})

// 添加滚动到最新的消息
const toDownPage = (): void => {
    nextTick(() => {
        // 获取聊天窗口DOM元素
        const chatWindow = document.querySelector('.flex-1.overflow-y-auto.p-4.space-y-4');
        if (chatWindow) {
            // 将滚动条滚动到底部，显示最新消息
            chatWindow.scrollTop = chatWindow.scrollHeight;
        }
    });
};

// 添加信息到聊天窗口 HACK 跟随 interface Message 更改 !!!
// 同时将信息添加到数据库
const addMessage = (chat: ChatWindow, messageData: Message): void => {
    // 向聊天窗口添加新消息
    chat.messages.push({
        id: Date.now(),                                  // 使用当前时间戳作为消息ID
        time: Date.now(),                               // 消息时间
        text: messageData.text,                         // 消息文本内容
        recommend: messageData?.recommend ?? [],        // 推荐回复列表，如果没有则为空数组
        rag: messageData?.rag ?? [],                    // RAG检索结果，如果没有则为空数组
        isStart: messageData?.isStart ?? false,         // 是否是开始消息
        isUser: messageData.isUser,                     // 是否是用户发送的消息
        useLlm: toRaw(chatModelName.value),             // 使用的LLM模型名称
        topInfo: getChatTopInfo(messageData.isUser)     // 获取消息顶部信息（时间和模型）
    });
    // 设置输入状态 - 如果是用户消息，表示正在等待AI回复
    isTyping.value = messageData.isUser
    // 滚动到最新消息
    toDownPage()
}

// 发送消息函数
const sendMessage = (recommend = null): void => {
    // 检查是否选择了模型
    if (!chatModelName.value) {
        ElMessage.error('请选择模型。')
        return
    }
    // 检查是否正在等待回复
    if (isTyping.value) {
        ElMessage.error('请等待回复完成。')
        return
    }
    // 如果是点击推荐问题，则将推荐内容填入输入框
    if (recommend) {
        inputMessageText.value = recommend;
    }
    // 查找当前聊天窗口
    const chat = chats.value.find(c => c.id === currentChatId.value);
    // 确保输入不为空且找到了聊天窗口
    if (inputMessageText.value.trim() && chat) {
        // 判断当前对话窗口是不是第一次提问问题，如果是则将问题设为标题
        if ((chat?.messages ?? []).length === 1) {
            chat.title = inputMessageText.value;
        }
        // 添加用户消息到聊天窗口
        addMessage(chat, {
            text: inputMessageText.value,
            isUser: true,
            topInfo: getChatTopInfo(true)
        })
        // 保存输入文本副本并清空输入框
        const inputTextCopy = inputMessageText.value;
        inputMessageText.value = '';
        // 创建取消令牌，用于取消请求
        axiosCancel = axios.CancelToken.source();
        // 发送HTTP请求到后端API
        http.request<any>({
            url: chatServiceType.value + '/chat',        // 请求URL，根据选择的聊天服务类型
            method: 'post',                             // 请求方法
            q_spinning: false,                          // 不显示加载动画
            cancelToken: axiosCancel.token,             // 取消令牌
            data: {
                id: chat.id,                            // 聊天ID
                p: inputPromptText.value,               // 提示词
                q: inputTextCopy,                       // 用户问题
                isRag: ragEnabled.value,                // 是否启用RAG
                isReRank: false,                // 是否启用rerank
                groupId: ragGroup.value,                // RAG组ID
                isFunction: toolEnabled.value,          // 是否启用函数调用
                modelName: chatModelName.value          // 模型名称
            }
        }).then((res) => {
            // 请求成功
            if (res.code === 200) {
                try {
                    // 尝试解析推荐回复（可能是JSON字符串）
                    addMessage(chat,
                        {
                            text: res.data.chat,                                             // AI回复文本
                            recommend: res.data.recommend && (JSON.parse(res.data.recommend)?.desc ?? []), // 推荐问题列表
                            rag: res.data?.rag ?? [],                                        // RAG检索结果
                            isUser: false,                                                   // 非用户消息（AI消息）
                            topInfo: getChatTopInfo(false)                                   // 消息顶部信息
                        }
                    )
                } catch (error) {
                    // 如果JSON解析失败，直接使用原始数据
                    addMessage(chat,
                        {
                            text: res.data.chat,
                            recommend: res.data.recommend,
                            rag: res.data?.rag ?? [],
                            isUser: false,
                            topInfo: getChatTopInfo(false)
                        }
                    )
                }
            } else {
                // 请求返回错误码
                addMessage(chat,
                    {
                        text: "回答出现错误，请换种方式提问。",
                        recommend: [],
                        isUser: false,
                        topInfo: getChatTopInfo(false)
                    }
                )
            }
        }).catch(error => {
            // 请求被取消
            if (error.code === "ERR_CANCELED") {
                addMessage(chat,
                    {
                        text: "请求已取消。",
                        recommend: [],
                        isUser: false,
                        topInfo: getChatTopInfo(false)
                    }
                )
            } else {
                // 其他错误
                console.warn(error, 'error');
                addMessage(chat,
                    {
                        text: "未知错误。",
                        recommend: [],
                        isUser: false,
                        topInfo: getChatTopInfo(false)
                    }
                )
            }
        })

    }
};
// 优化替换提示词到输入框 - 使用AI优化用户输入的提示词
const optimizePromptWords = (): void => {
    // 发送请求获取优化后的提示词
    http.request<any>({
        url: '/chat/getPrompt',           // 获取提示词的API端点
        method: 'post',                  // 请求方法
        q_spinning: true,                // 显示加载动画
        data: {
            q: inputMessageText.value     // 发送当前输入框中的文本
        }
    }).then((res) => {
        // 如果请求成功，用优化后的提示词替换输入框内容
        if (res.code === 200) {
            inputMessageText.value = res.data
        }
    })
}

// 获取RAG文件组列表 - 检索增强生成的文件组选项
const getRagGroupOptionList = (): Promise<any> => {
    return http.request<any>({
        url: '/rag/file-group/getEnum',   // 获取RAG文件组枚举的API端点
        method: 'post',                  // 请求方法
        q_spinning: true,                // 显示加载动画
        data: {},                        // 无需传递数据
    }).then((res) => {
        if (res.code === 200) {
            // 将返回的数据转换为选项格式
            ragGroupOption.value = res.data.map((e: any) => {
                return {
                    label: e.groupName,    // 显示名称
                    value: e.id            // 选项值
                }
            })
            // 默认选择第一个选项
            ragGroup.value = ragGroupOption.value[0].value
        }
    })
}

// 停止当前对话 - 取消正在进行的请求
const messageStop = (): void => {
    if (axiosCancel) {
        // 如果存在取消令牌，则取消请求
        axiosCancel.cancel();
    }
}

// 保存对话记忆 - 将当前对话保存到服务器
const saveChatMemory = () => {
    return http.request<any>({
        url: '/chat/saveChatMemory',     // 保存对话记忆的API端点
        method: 'post',                  // 请求方法
        q_spinning: true,                // 显示加载动画
        data: {
            id: currentChatId.value,      // 当前对话ID
            name:'1'
        }
    }).then((res: any) => {
        // 显示成功消息
        ElMessage.success(res.data)
    })

};

// 清空对话记忆并删除对话框
const clearChatMemory = (id = ''): void => {
    http.request<any>({
        url: '/chat/clearChatMemory',    // 清除对话记忆的API端点
        method: 'post',                  // 请求方法
        q_spinning: true,                // 显示加载动画
        data: {
            id: id,                       // 要清除的对话ID
        }
    }).then((res: any) => {
        // 从聊天列表中移除该对话
        chats.value = chats.value.filter(c => c.id !== id);

        // 获取最后一个对话的ID，如果没有则为false
        const lastId = chats.value[chats.value.length - 1]?.id ?? false;
        if (lastId) {
            // 如果还有其他对话，切换到最后一个对话
            switchChat(false, lastId);
        } else {
            // 如果没有对话了，创建一个新对话
            startNewChat()
        }
        // 显示成功消息
        ElMessage.success(res.data)
    })
};

// 开始一个新的对话
const startNewChat = (): void => {
    // 创建唯一的对话ID
    const newChatId = generateUUID();
    // 设置当前对话ID
    currentChatId.value = newChatId;
    // 将新对话添加到对话列表
    chats.value.push({
        id: newChatId,
        title: '新对话',
        messages: [],
        active: false
    });
    // 查找新创建的对话
    const chat = chats.value.find(c => c.id === currentChatId.value);
    // 如果找到对话，添加欢迎消息
    chat && addMessage(chat, {
        text: '很高兴见到你！我可以帮你写代码、读文件、写作各种创意内容，请把你的任务交给我吧~',
        recommend: ['你是谁？', '你能做些什么？'],  // 推荐问题
        isStart: true,                             // 标记为开始消息
        rag: [],                                   // 无RAG结果
        isUser: false,                             // 非用户消息（AI消息）
        topInfo: getChatTopInfo(false)             // 获取消息顶部信息
    })
};

// 切换对话窗口并加载对话历史
const switchChat = async (isGetMemory: boolean, chatId: string) => {
    // 如果当前已经是这个对话，则不做任何操作
    if (currentChatId.value === chatId) {
        return;
    }
    // 设置当前对话ID
    currentChatId.value = chatId;
    // 如果需要获取记忆，则从服务器加载对话历史
    isGetMemory && await http.request<any>({
        url: '/chat/getChatMemory',      // 获取对话记忆的API端点
        method: 'post',                  // 请求方法
        q_spinning: true,                // 显示加载动画
        data: {
            id: currentChatId.value,      // 当前对话ID
        }
    }).then((res: any) => {
        if (res.code === 200) {
            // 找到当前对话
            let chat = chats.value.find(c => c.id === currentChatId.value);
            // 设置对话标题为倒数第二条消息的内容（通常是用户的最后一个问题）
            chat!.title = res.data[res.data.length - 2].text
            // 清空消息列表，准备重新加载
            chat!.messages = []
            if (chat) {
                // 遍历返回的消息数据，添加到对话中
                res.data.forEach((e: any) => {
                    addMessage(chat, {
                        text: e.text,                     // 消息文本
                        recommend: [],                    // 无推荐问题
                        rag: [],                          // 无RAG结果
                        isUser: e.messageType === 'USER', // 根据消息类型判断是否为用户消息
                        topInfo: ''                       // 无顶部信息
                    })
                })
            }
        }
    })
    // 滚动到最新消息
    toDownPage()
};

// 获取当前活跃的聊天窗口
const currentChat = (): ChatWindow | any => {
    // 查找当前ID对应的聊天窗口，如果没有则返回空对象
    return chats.value.find(c => c.id === currentChatId.value) || { messages: [] };
};

// 获取消息顶部的时间和模型信息
const getChatTopInfo = (isUser: boolean): string => {
    // 获取当前日期时间的格式化字符串
    const getCurrentDateTime = (): string => {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0'); // 月份从0开始，需要+1
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    };

    // 用户消息只显示时间，AI消息显示模型名称和时间
    return isUser ? `${getCurrentDateTime()}` : `${toRaw(chatModelName.value)}：      ${getCurrentDateTime()}`
};

// RAG功能启用状态变化处理
const ragEnabledChange = (e: any): void => {
    // 清空当前选择的RAG组
    ragGroup.value = ''
}

// 聊天服务类型变化处理
const chatServiceTypeChange = (e: any): void => {
    // 如果选择了'/onlyRag'服务类型，自动启用RAG功能
    if (e === '/onlyRag') {
        ragEnabled.value = true
    }
}

// 打开RAG引用文档查看对话框
const openRagDocumentView = (rag: any[]) => {
    // 设置文档数据
    aiRagDocumentData.value = rag
    // 显示文档对话框
    aiRagDocumentVisible.value = true
}

// 文档评分处理（点赞/点踩）
const documentRankHandle = async (t: 'good' | 'bad', d: any) => {
    // 调用API进行评分
    const res: any = await documentRankHandleApi(d.id, d.metadata.rank, t)
    if (res.code === 200) {
        // 显示成功消息
        ElMessage.success(res.data)
    }
}
// 反馈相关状态变量
let feedbackVisible = ref<boolean>(false);   // 反馈对话框是否可见
let feedbackText = ref<string>('');          // 反馈文本内容
let feedbackCache = ref<any>();              // 缓存当前反馈的消息和类型

// 打开反馈对话框 - 用于用户对AI回复进行评价
const chatFeedback = (message: Message, type: string) => {
    // 显示反馈对话框
    feedbackVisible.value = true;
    // 清空反馈文本
    feedbackText.value = '';
    // 缓存消息对象和反馈类型（like或dislike）
    feedbackCache.value = { ...message, type: type };
}

// 提交用户反馈到服务器
const submitFeedback = () => {
    // 注释掉的代码是验证反馈内容不能为空的逻辑
    // if (feedbackText.value.trim() === '') {
    //     ElMessage.error('请输入反馈内容');
    //     return;
    // }

    // 获取当前聊天窗口的所有消息
    const nowChat = currentChat().messages
    // 找到被反馈消息的索引
    const nowMessageIndex = nowChat.findIndex((item: any) => item.id === feedbackCache.value?.id);
    // 获取用户的问题（通常是反馈消息的前一条）
    const question = nowChat[nowMessageIndex - 1]?.text;
    // 获取AI的回答（当前被反馈的消息）
    const answer = nowChat[nowMessageIndex]?.text;

    // 发送反馈到服务器
    http.request<any>({
        url: '/chatFeedback/add',           // 添加反馈的API端点
        method: 'post',                    // 请求方法
        q_spinning: true,                  // 显示加载动画
        q_contentType: 'json',             // 内容类型为JSON
        data: {
            questionId: feedbackCache.value?.id,                                      // 问题ID
            questionContent: question,                                                 // 问题内容
            answerContent: answer,                                                     // 回答内容
            interaction: feedbackCache.value?.type ?? 'dislike',                       // 交互类型（喜欢/不喜欢）
            knowledgeBaseIds: feedbackCache.value?.rag?.map((e: any) => e.id).join(','), // 相关知识库IDs
            suggestion: feedbackText.value,                                           // 用户建议文本
        },

    }).then((res) => {
        if (res.code === 200) {
            // 请求成功，关闭对话框并清空状态
            feedbackVisible.value = false;
            feedbackText.value = '';
            feedbackCache.value = void 0;
            // 显示成功消息
            ElMessage.success('反馈已提交，谢谢！');
        }
    }).catch((error) => {
        // 请求失败，显示错误消息
        ElMessage.error('提交反馈失败，请重试。');
    });
}

// 初始化对话记忆 - 从服务器加载已有的对话窗口
const initChatMemory = async () => {
    // 获取所有保存的对话窗口ID
    const chatWindows = await http.request<any>({
        url: '/chat/getChatMemoryWindows',  // 获取对话窗口列表的API端点
        method: 'post',                     // 请求方法
        q_spinning: true,                   // 显示加载动画
    })
    if (chatWindows.data.length > 0) {
        // 如果有保存的对话窗口
        for (let index = 0; index < chatWindows.data.length; index++) {
            const e = chatWindows.data[index];
            // 添加对话窗口到列表
            chats.value.push({
                id: e.conversationId,                        // 对话ID
                title: e.conversationName,              // 默认标题
                messages: [],                 // 空消息列表
                active: false                 // 非活跃状态
            });
            // 切换到该对话并加载历史记录
            await switchChat(true, e.conversationId)
        }
    } else {
        // 如果没有保存的对话窗口，创建一个新对话
        startNewChat()
        // 切换到新创建的对话
        await switchChat(false, chats.value[0].id)
    }
}

// 组件初始化执行
nextTick(async () => {
    // if (userStore.isAdmin) {
    //     router.push('/admin')
    //     return;
    // }
    // 设置AI模型选项
    await aimodel().setModelOptions()
    // 获取模型选项列表
    chatModelOption.value = toRaw(await aimodel().getModelOptions()) as any
    // 默认选择第一个模型
    chatModelName.value = chatModelOption.value[0].value
    // 获取RAG文件组选项列表
    getRagGroupOptionList()
    // 初始化对话记忆
    await initChatMemory()
})

</script>

<template>
    <div :class="nowIsChat
        ? 'h-screen w-full bg-white flex gap-4 relative'
        : 'h-full w-full bg-white flex gap-4 relative'">

        <!-- 对话窗口列表 -->
        <div :class="[
            'w-64 bg-white rounded-lg shadow-lg p-4 transition-transform duration-300',
            'fixed md:static h-full] z-40',
        ]">
            <div class="flex justify-between items-center mb-4">
                <h2 class="text-lg font-semibold">对话列表</h2>
                <el-button @click="startNewChat()"
                    class="!px-3 !py-2 !bg-blue-500 !text-white hover:!bg-blue-600 active:!bg-blue-700 transition-colors duration-200">
                    新建对话
                </el-button>
            </div>
            <ul class="space-y-2 overflow-y-auto" style="height: calc(100% - 32px - 64px);">
                <li v-for="chat in chats" :key="chat.id" :class="[
                    'p-2 rounded-lg transition-colors duration-200 flex justify-between items-center',
                    chat.id === currentChatId ? 'bg-blue-100' : 'hover:bg-gray-100'
                ]">
                    <span class="cursor-pointer overflow-hidden text-ellipsis whitespace-nowrap"
                        style="max-width: calc(100% - 70px); display: inline-block;"
                        @click="switchChat(false, chat.id)">
                        {{ chat.title }}
                    </span>
                    <span class="text-blue-500 cursor-pointer w-10" @click="clearChatMemory(chat.id)">清除</span>
                </li>
            </ul>
            <!--                            {{ message.text }}-->
            <div class="mt-4 absolute bottom-4 left-2 bg-white w-60">
                <!-- <el-button @click="aiOptionVisible = true" class="w-full !bg-blue-500 hover:!bg-green-600">
                    <span class="font-medium text-white">
                        设置
                    </span>
                </el-button> -->
                <el-button v-if="nowIsChat" @click="userStore.userLogOut()"
                    class="w-full !bg-blue-500 hover:!bg-green-600">
                    <span class="font-medium text-white">
                        登出
                    </span>
                </el-button>
            </div>
        </div>

        <!-- 对话窗口 -->
        <div v-if="chats.length > 0" class="flex-1 bg-white rounded-lg shadow-lg flex flex-col h-full">
            <div class="p-4 pb-3 border-b-2  border-gray-300">
                欢迎来到 {{ BASE_PROJECT_NAME }}
                <!-- #{{ currentChatId }} -->
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

                        <div :class="message.isUser ? 'flex items-end justify-end' : 'flex items-end justify-start'">
                            <!-- <div v-html="message.text" :class="[
                                'max-full p-3 rounded-lg transition-all duration-200',
                                message.isUser ? 'bg-blue-500 text-white' : 'bg-gray-200'
                            ]"></div> -->
                            <v-md-preview :text="message.text"></v-md-preview>

                            <img v-if="!message.isStart && !message.isUser" class="cursor-pointer"
                                @click="chatFeedback(message, 'like')" style="height: 20px;margin: 0 8px;"
                                src="./good.svg" alt="" srcset="">
                            <img v-if="!message.isStart && !message.isUser" class="cursor-pointer"
                                @click="chatFeedback(message, 'dislike')" style="height: 20px;" src="./bad.svg" alt=""
                                srcset="">
                        </div>
                        <div class="flex items-center justify-start ">
                            <!--                        推荐列表-->
                            <div v-if="(message?.recommend ?? []).length > 0"
                                class="w-min flex justify-start items-center mt-1">
                                <div class="bg-blue-100 w-max mr-2 px-1.5 py-0.5 text-[14px] rounded-md cursor-pointer"
                                    v-for="item in message.recommend" :key="item" @click="sendMessage(item)">
                                    {{ item }}
                                </div>
                            </div>
                            <!--                        引用的知识 -->
                            <div>
                                <el-button v-if="(message?.rag ?? []).length > 0" style="padding-bottom: 0px;"
                                    :key="'text'" @click="openRagDocumentView(message.rag)" text>引用的知识库</el-button>
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
                        <el-button @click="saveChatMemory()" :disabled="isTyping" type="primary">
                            <span class="font-medium text-white">
                                保存记录
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
                    <!-- <el-switch v-model="toolEnabled" inactive-text="工具" :disabled="isTyping" :active-value="true"
                        :inactive-value="false" active-color="#3b82f6" inactive-color="#dc2626" class="mr-2" />
                    <el-select v-if="toolEnabled" v-model="toolGroup" :disabled="isTyping" placeholder="选择工具组"
                        class="mr-2">
                        <el-option v-for="item in toolGroupOption" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select> -->
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
                        <el-tag type="">排名：{{ item.metadata.rank }}</el-tag>
                        <el-tag type="">距离：{{ item.metadata.distance }}</el-tag>
                        <el-tag type="">文件名：{{ item.metadata.filename }}</el-tag>
                        <el-icon style="font-size: 18px;margin: 0 4px; cursor: pointer;"
                            @click="documentRankHandle('good', item)">
                            <Top />
                        </el-icon>
                        <el-icon style="font-size: 18px; cursor: pointer;" @click="documentRankHandle('bad', item)">
                            <Bottom />
                        </el-icon>
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
        <!-- 反馈区域 -->
        <el-dialog v-model="feedbackVisible" title="反馈" width="600">
            <div class="feedback-area">
                <el-input v-model="feedbackText" type="textarea" placeholder="输入反馈内容或者您想要的结果，这对我们很重要..."></el-input>
                <div class="flex justify-end mt-4">
                    <el-button @click="submitFeedback" type="primary">提交反馈</el-button>
                </div>
            </div>
        </el-dialog>
    </div>
</template>
