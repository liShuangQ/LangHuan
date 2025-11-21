<!-- 聊天侧边栏组件 -->
<script setup lang="ts">
// 导入Vue核心功能
import { ref, computed, nextTick } from "vue";
// 导入响应式断点工具
import { useBreakpoints } from "@vueuse/core";
// 导入消息通知组件
import MessageNotification from "@/components/MessageNotification/index.vue";

// 聊天项目接口定义
interface ChatItem {
    id: string;
    title: string;
    date: string;
    active?: boolean;
}

// 事件类型定义
interface EmitEvent {
    (event: "action", type: ActionType, payload?: any): void;
}

// 组件属性定义
const props = defineProps<{
    chatList: ChatItem[];
}>();

// 事件发射器
const emit = defineEmits<EmitEvent>();

// 响应式断点配置
const breakpoints = useBreakpoints({
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
})

// 移动端检测和状态管理
const isMobile = breakpoints.smaller('sm')
const isMobileSidebarOpen = ref<boolean>(false)
let historyWindowShow = ref<boolean>(true)

/**
 * 处理侧边栏操作事件
 * @param type 操作类型
 * @param payload 操作参数
 */
const handleAction = (type: ActionType, payload?: any) => {
    if (type === 'barChat') {
        // 处理侧边栏显示/隐藏切换
        if (isMobile.value) {
            isMobileSidebarOpen.value = !isMobileSidebarOpen.value
        } else {
            historyWindowShow.value = !historyWindowShow.value
        }
        return;
    }
    emit("action", type, payload);
};

// 手机默认不打开（已注释的代码）
// if (isMobile.value) {
//     handleAction('settings')
// }

/**
 * 打开移动端侧边栏
 */
const openMobileSidebar = () => {
    isMobileSidebarOpen.value = true
}

/**
 * 关闭移动端侧边栏
 */
const closeMobileSidebar = () => {
    isMobileSidebarOpen.value = false
}

// 项目基础信息
const BASE_PROJECT_NAME = computed(() => {
    return process.env.BASE_PROJECT_NAME as string;
});
const BASE_PROJECT_LOGO_URL_SMALL = computed(() => {
    return process.env.BASE_PROJECT_LOGO_URL_SMALL as string;
});

// 操作类型枚举
type ActionType =
    | "new-chat"      // 新建聊天
    | "chat"          // 选择聊天
    | "discover"      // 发现功能
    | "logout"        // 用户登出
    | "delete"        // 删除聊天
    | "barChat"       // 侧边栏聊天
    | "settings"      // 设置
    | "editend";      // 编辑完成

// 搜索和编辑相关状态
const searchQuery = ref("");
const editingChatId = ref<string | null>(null);
const editingTitle = ref("");

/**
 * 根据搜索查询过滤聊天列表
 */
const filteredChatList = computed(() => {
    const query = searchQuery.value.toLowerCase().trim();
    if (!query) return props.chatList;

    return props.chatList.filter(
        (chat) =>
            chat.title.toLowerCase().includes(query) ||
            chat.date.toLowerCase().includes(query)
    );
});

/**
 * 开始编辑聊天标题
 * @param chatId 聊天ID
 * @param currentTitle 当前标题
 */
const startEdit = (chatId: string, currentTitle: string) => {
    editingChatId.value = chatId;
    editingTitle.value = currentTitle;
    nextTick(() => {
        const input = document.querySelector(
            'input[ref="editInput"]'
        ) as HTMLInputElement;
        if (input) {
            input.focus();
            input.select();
        }
    });
};

/**
 * 完成编辑聊天标题
 * @param chatId 聊天ID
 */
const finishEdit = (chatId: string) => {
    editingChatId.value = null;
    if (editingTitle.value.trim()) {
        handleAction("editend", {
            id: chatId,
            title: editingTitle.value.trim(),
        });
    }
    editingTitle.value = "";
};

/**
 * 取消编辑
 */
const cancelEdit = () => {
    editingChatId.value = null;
    editingTitle.value = "";
};

/**
 * 处理键盘事件
 * @param event 键盘事件
 * @param chatId 聊天ID
 */
const handleKeydown = (event: KeyboardEvent, chatId: string) => {
    if (event.key === "Enter") {
        finishEdit(chatId);
    } else if (event.key === "Escape") {
        cancelEdit();
    }
};


</script>

<template>
    <!-- Desktop Sidebar -->
    <aside v-if="!isMobile" class="flex z-10">
        <!-- First Column -->
        <div
            class="flex h-full flex-col items-center space-y-8 border-r border-slate-300 bg-slate-50 py-8 dark:border-slate-700 dark:bg-slate-900 w-16 sm:w-16 ">
            <!-- Logo -->
            <div v-if="BASE_PROJECT_LOGO_URL_SMALL" class="w-full">
                <img :src="BASE_PROJECT_LOGO_URL_SMALL" alt="logo" class="w-full"></img>
            </div>
            <div v-else class="w-full">
            </div>

            <!-- Navigation Buttons -->
            <a href="#" @click.prevent="handleAction('new-chat')"
                class="rounded-lg p-1.5 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 24 24" stroke-width="2"
                    stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                    <path d="M8 9h8"></path>
                    <path d="M8 13h6"></path>
                    <path d="M12.01 18.594l-4.01 2.406v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v5.5">
                    </path>
                    <path d="M16 19h6"></path>
                    <path d="M19 16v6"></path>
                </svg>
            </a>

            <a href="#" @click.prevent="handleAction('barChat')"
                class="rounded-lg p-1.5 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 24 24" stroke-width="2"
                    stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                    <path d="M21 14l-3 -3h-7a1 1 0 0 1 -1 -1v-6a1 1 0 0 1 1 -1h9a1 1 0 0 1 1 1v10"></path>
                    <path d="M14 15v2a1 1 0 0 1 -1 1h-7l-3 3v-10a1 1 0 0 1 1 -1h2"></path>
                </svg>
            </a>

            <MessageNotification :size="24" :placement="'right-start'"></MessageNotification>

            <a href="#" @click.prevent="handleAction('settings')"
                class="rounded-lg p-1.5 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 24 24" stroke-width="2"
                    stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                    <path
                        d="M19.875 6.27a2.225 2.225 0 0 1 1.125 1.948v7.284c0 .809 -.443 1.555 -1.158 1.948l-6.75 4.27a2.269 2.269 0 0 1 -2.184 0l-6.75 -4.27a2.225 2.225 0 0 1 -1.158 -1.948v-7.285c0 -.809 .443 -1.554 1.158 -1.947l6.75 -3.98a2.33 2.33 0 0 1 2.25 0l6.75 3.98h-.033z">
                    </path>
                    <path d="M12 12m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0"></path>
                </svg>
            </a>

            <a href="#" @click.prevent="handleAction('logout')"
                class="rounded-lg p-1.5 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 icon" viewBox="0 0 1024 1024" stroke-width="0"
                    stroke="none" fill="none" stroke-linecap="round" stroke-linejoin="round">
                    <path
                        d="M904.832 512.192l-252.352 252.352-60.352-60.352 149.376-149.312h-408.96V469.568h408.96L592.128 320.192l60.352-60.288 252.352 252.288z m-487.04-320H204.48v640h213.312v85.376H119.168V106.88h298.624v85.312z"
                        p-id="5144" fill="currentColor"></path>
                </svg>
            </a>
        </div>
        <!-- Second Column -->
        <div v-show="historyWindowShow" class="h-screen overflow-y-auto bg-slate-50 py-8 dark:bg-slate-900 w-60">
            <div class="flex items-start">
                <h2 class="inline px-5 text-lg font-medium text-slate-800 dark:text-slate-200">
                    {{ BASE_PROJECT_NAME }}
                </h2>
                <!-- <span class="rounded-full bg-blue-600 px-2 py-1 text-xs text-slate-200">
                    24
                </span> -->
            </div>

            <div class="mx-2 mt-8 space-y-4">
                <form @submit.prevent>
                    <label for="search-chats" class="sr-only">Search chats</label>
                    <div class="relative">
                        <input id="search-chats" type="text" v-model="searchQuery"
                            class="w-full rounded-lg border border-slate-300 bg-slate-50 p-3 pr-10 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200"
                            placeholder="Search chats" />
                        <button type="submit"
                            class="absolute bottom-2 right-2.5 rounded-lg p-2 text-sm text-slate-500 hover:text-blue-700 focus:outline-none sm:text-base">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" aria-hidden="true"
                                viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none"
                                stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path d="M8 9h8"></path>
                                <path d="M8 13h5"></path>
                                <path
                                    d="M11.008 19.195l-3.008 1.805v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v4.5">
                                </path>
                                <path d="M18 18m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0"></path>
                                <path d="M20.2 20.2l1.8 1.8"></path>
                            </svg>
                            <span class="sr-only">Search chats</span>
                        </button>
                    </div>
                </form>

                <div v-for="chat in filteredChatList" :key="chat.id" :class="[
                    'group flex w-full items-center justify-between gap-y-2 rounded-lg px-3 py-2 text-left transition-colors duration-200 focus:outline-none relative',
                    chat.active
                        ? 'bg-slate-200 dark:bg-slate-800'
                        : 'hover:bg-slate-200 dark:hover:bg-slate-800',
                ]">
                    <div class="flex-1" :class="{ 'cursor-pointer': editingChatId !== chat.id }" @click="
                        editingChatId !== chat.id &&
                        handleAction('chat', chat.id)
                        ">
                        <h1 v-if="editingChatId !== chat.id"
                            class="text-sm font-medium capitalize text-slate-700 dark:text-slate-200">
                            {{ chat.title }}
                        </h1>
                        <input v-else v-model="editingTitle" @blur="finishEdit(chat.id)"
                            @keydown="handleKeydown($event, chat.id)"
                            class="text-sm font-medium capitalize text-slate-700 dark:text-slate-200 bg-transparent border-b border-blue-500 focus:outline-none w-full"
                            ref="editInput" />
                        <p class="text-xs text-slate-500 dark:text-slate-400">
                            {{ chat.date }}
                        </p>
                    </div>

                    <div class="items-center space-x-1 absolute right-2">
                        <button @click.stop="startEdit(chat.id, chat.title)"
                            class="rounded p-1 hover:bg-blue-500 hover:text-white">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" stroke-width="2"
                                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                                <path d="M7 7h-1a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2h9a2 2 0 0 0 2 -2v-1" />
                                <path d="M20.385 6.585a2.1 2.1 0 0 0 -2.97 -2.97l-8.415 8.385v3h3l8.385 -8.415z" />
                                <path d="M16 5l3 3" />
                            </svg>
                        </button>
                        <button @click.stop="handleAction('delete', chat.id)"
                            class="rounded p-1 hover:bg-red-500 hover:text-white">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" stroke-width="2"
                                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                                <path d="M4 7h16" />
                                <path d="M10 11l0 6" />
                                <path d="M14 11l0 6" />
                                <path d="M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12" />
                                <path d="M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3" />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </aside>

    <!-- Mobile Floating Button and Sidebar -->
    <div v-else>
        <!-- Floating Button -->
        <button @click="openMobileSidebar"
            class="fixed top-4 left-4 z-50 rounded-md p-2 bg-blue-600 text-white shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors duration-200">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" stroke-width="2"
                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M4 6h16"></path>
                <path d="M4 12h16"></path>
                <path d="M4 18h16"></path>
            </svg>
            <span class="sr-only">Open sidebar</span>
        </button>

        <!-- Mobile Sidebar Drawer -->
        <div v-if="isMobileSidebarOpen" class="fixed inset-0 z-50 flex w-[270px]">
            <!-- Backdrop -->
            <div @click="closeMobileSidebar" class="absolute inset-0 bg-black bg-opacity-50"></div>

            <!-- Sidebar Content -->
            <div class="relative flex h-full w-80 max-w-full flex-col bg-white dark:bg-slate-900 shadow-xl">
                <!-- Close Button -->
                <button @click="closeMobileSidebar"
                    class="absolute top-4 right-4 z-10 rounded-lg p-2 bg-slate-200 dark:bg-slate-700 text-slate-500 hover:bg-slate-300 dark:hover:bg-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" stroke-width="2"
                        stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                        <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                        <path d="M18 6l-12 12"></path>
                        <path d="M6 6l12 12"></path>
                    </svg>
                    <span class="sr-only">Close sidebar</span>
                </button>

                <!-- Logo and Navigation -->
                <div class="flex h-full flex-col">
                    <!-- Logo Section -->
                    <div
                        class="flex flex-col items-center space-y-4 border-b border-slate-200 dark:border-slate-700 py-6">
                        <h2 class="text-lg font-medium text-slate-800 dark:text-slate-200">
                            {{ BASE_PROJECT_NAME }}
                        </h2>
                    </div>

                    <!-- Navigation Buttons -->
                    <div class="flex justify-around border-b border-slate-200 dark:border-slate-700 py-2">
                        <a href="#" @click.prevent="handleAction('new-chat'); closeMobileSidebar();"
                            class="rounded-lg p-2 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 24 24" stroke-width="2"
                                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path d="M8 9h8"></path>
                                <path d="M8 13h6"></path>
                                <path
                                    d="M12.01 18.594l-4.01 2.406v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v5.5">
                                </path>
                                <path d="M16 19h6"></path>
                                <path d="M19 16v6"></path>
                            </svg>
                        </a>

                        <a href="#" @click.prevent="handleAction('settings'); closeMobileSidebar();"
                            class="rounded-lg p-2 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 24 24" stroke-width="2"
                                stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                <path
                                    d="M19.875 6.27a2.225 2.225 0 0 1 1.125 1.948v7.284c0 .809 -.443 1.555 -1.158 1.948l-6.75 4.27a2.269 2.269 0 0 1 -2.184 0l-6.75 -4.27a2.225 2.225 0 0 1 -1.158 -1.948v-7.285c0 -.809 .443 -1.554 1.158 -1.947l6.75 -3.98a2.33 2.33 0 0 1 2.25 0l6.75 3.98h-.033z">
                                </path>
                                <path d="M12 12m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0"></path>
                            </svg>
                        </a>

                        <a href="#" @click.prevent="handleAction('logout')"
                            class="rounded-lg p-2 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 icon" viewBox="0 0 1024 1024"
                                stroke-width="0" stroke="none" fill="none" stroke-linecap="round"
                                stroke-linejoin="round">
                                <path
                                    d="M904.832 512.192l-252.352 252.352-60.352-60.352 149.376-149.312h-408.96V469.568h408.96L592.128 320.192l60.352-60.288 252.352 252.288z m-487.04-320H204.48v640h213.312v85.376H119.168V106.88h298.624v85.312z"
                                    p-id="5144" fill="currentColor"></path>
                            </svg>
                        </a>
                    </div>

                    <!-- Chat List -->
                    <div class="flex-1 overflow-y-auto py-4">
                        <div class="mx-2 space-y-4">
                            <!-- Search -->
                            <form @submit.prevent>
                                <label for="mobile-search-chats" class="sr-only">Search chats</label>
                                <div class="relative">
                                    <input id="mobile-search-chats" type="text" v-model="searchQuery"
                                        class="w-full rounded-lg border border-slate-300 bg-slate-50 p-3 pr-10 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200"
                                        placeholder="Search chats" />
                                    <button type="submit"
                                        class="absolute bottom-2 right-2.5 rounded-lg p-2 text-sm text-slate-500 hover:text-blue-700 focus:outline-none sm:text-base">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" aria-hidden="true"
                                            viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none"
                                            stroke-linecap="round" stroke-linejoin="round">
                                            <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                                            <path d="M8 9h8"></path>
                                            <path d="M8 13h5"></path>
                                            <path
                                                d="M11.008 19.195l-3.008 1.805v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v4.5">
                                            </path>
                                            <path d="M18 18m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0"></path>
                                            <path d="M20.2 20.2l1.8 1.8"></path>
                                        </svg>
                                        <span class="sr-only">Search chats</span>
                                    </button>
                                </div>
                            </form>

                            <!-- Chat Items -->
                            <div v-for="chat in filteredChatList" :key="chat.id" :class="[
                                'group flex w-full items-center justify-between gap-y-2 rounded-lg px-3 py-3 text-left transition-colors duration-200 focus:outline-none relative',
                                chat.active
                                    ? 'bg-slate-200 dark:bg-slate-800'
                                    : 'hover:bg-slate-200 dark:hover:bg-slate-800',
                            ]">
                                <div class="flex-1" :class="{ 'cursor-pointer': editingChatId !== chat.id }"
                                    @click.stop="
                                        editingChatId !== chat.id &&
                                        handleAction('chat', chat.id) && closeMobileSidebar()
                                        ">
                                    <h1 v-if="editingChatId !== chat.id"
                                        class="text-sm font-medium capitalize text-slate-700 dark:text-slate-200">
                                        {{ chat.title }}
                                    </h1>
                                    <input v-else v-model="editingTitle" @blur="finishEdit(chat.id)"
                                        @keydown="handleKeydown($event, chat.id)"
                                        class="text-sm font-medium capitalize text-slate-700 dark:text-slate-200 bg-transparent border-b border-blue-500 focus:outline-none w-full"
                                        ref="editInput" />
                                    <p class="text-xs text-slate-500 dark:text-slate-400">
                                        {{ chat.date }}
                                    </p>
                                </div>

                                <div class="flex items-center space-x-1 absolute right-2">
                                    <button @click.stop="startEdit(chat.id, chat.title)"
                                        class="rounded p-1 hover:bg-blue-500 hover:text-white">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24"
                                            stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round"
                                            stroke-linejoin="round">
                                            <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                                            <path d="M7 7h-1a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2h9a2 2 0 0 0 2 -2v-1" />
                                            <path
                                                d="M20.385 6.585a2.1 2.1 0 0 0 -2.97 -2.97l-8.415 8.385v3h3l8.385 -8.415z" />
                                            <path d="M16 5l3 3" />
                                        </svg>
                                    </button>
                                    <button @click.stop="handleAction('delete', chat.id)"
                                        class="rounded p-1 hover:bg-red-500 hover:text-white">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24"
                                            stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round"
                                            stroke-linejoin="round">
                                            <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                                            <path d="M4 7h16" />
                                            <path d="M10 11l0 6" />
                                            <path d="M14 11l0 6" />
                                            <path d="M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12" />
                                            <path d="M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3" />
                                        </svg>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
