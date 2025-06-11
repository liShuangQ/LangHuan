<script setup lang="ts">
import { ref, computed } from 'vue'
import MessageNotification from "@/components/MessageNotification/index.vue"
interface ChatItem {
    id: string;
    title: string;
    date: string;
    active?: boolean;
}

interface EmitEvent {
    (event: 'action', type: ActionType, payload?: any): void;
}

const props = defineProps<{
    chatList: ChatItem[];
}>();

const emit = defineEmits<EmitEvent>();

const handleAction = (type: ActionType, payload?: any) => {
    emit('action', type, payload);
};

const BASE_PROJECT_NAME = computed(() => {
    return process.env.BASE_PROJECT_NAME as string
})
type ActionType = 'new-chat' | 'chat' | 'discover' | 'logout' | 'delete' | 'barChat' | 'settings';

const searchQuery = ref('');

const filteredChatList = computed(() => {
    const query = searchQuery.value.toLowerCase().trim();
    if (!query) return props.chatList;

    return props.chatList.filter(chat =>
        chat.title.toLowerCase().includes(query) ||
        chat.date.toLowerCase().includes(query)
    );
});



</script>

<template>
    <aside class="flex">
        <!-- First Column -->
        <div
            class="flex h-full w-full flex-col items-center space-y-8 border-r border-slate-300 bg-slate-50 py-8 dark:border-slate-700 dark:bg-slate-900 sm:w-16">
            <!-- Logo -->
            <a href="#" class="mb-1">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-7 w-7 text-blue-600" fill="currentColor"
                    stroke-width="1" viewBox="0 0 24 24">
                    <path
                        d="M20.553 3.105l-6 3C11.225 7.77 9.274 9.953 8.755 12.6c-.738 3.751 1.992 7.958 2.861 8.321A.985.985 0 0012 21c6.682 0 11-3.532 11-9 0-6.691-.9-8.318-1.293-8.707a1 1 0 00-1.154-.188zm-7.6 15.86a8.594 8.594 0 015.44-8.046 1 1 0 10-.788-1.838 10.363 10.363 0 00-6.393 7.667 6.59 6.59 0 01-.494-3.777c.4-2 1.989-3.706 4.728-5.076l5.03-2.515A29.2 29.2 0 0121 12c0 4.063-3.06 6.67-8.046 6.965zM3.523 5.38A29.2 29.2 0 003 12a6.386 6.386 0 004.366 6.212 1 1 0 11-.732 1.861A8.377 8.377 0 011 12c0-6.691.9-8.318 1.293-8.707a1 1 0 011.154-.188l6 3A1 1 0 018.553 7.9z">
                    </path>
                </svg>
            </a>

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
                class="rounded-lg p-1.5 transition-colors duration-200 focus:outline-none bg-blue-100 text-blue-600 dark:bg-slate-800 dark:text-blue-600">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 24 24" stroke-width="2"
                    stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                    <path d="M21 14l-3 -3h-7a1 1 0 0 1 -1 -1v-6a1 1 0 0 1 1 -1h9a1 1 0 0 1 1 1v10"></path>
                    <path d="M14 15v2a1 1 0 0 1 -1 1h-7l-3 3v-10a1 1 0 0 1 1 -1h2"></path>
                </svg>
            </a>

            <!-- <a href="#" @click.prevent="handleAction('discover')" class="rounded-lg p-1.5 transition-colors duration-200 focus:outline-none text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-800">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                    <path d="M10 10m-7 0a7 7 0 1 0 14 0a7 7 0 1 0 -14 0"></path>
                    <path d="M21 21l-6 -6"></path>
                </svg>
            </a> -->

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
        <div class="h-screen w-52 overflow-y-auto bg-slate-50 py-8 dark:bg-slate-900 sm:w-60">
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
                    'group flex w-full items-center justify-between gap-y-2 rounded-lg px-3 py-2 text-left transition-colors duration-200 focus:outline-none',
                    chat.active ? 'bg-slate-200 dark:bg-slate-800' : 'hover:bg-slate-200 dark:hover:bg-slate-800'
                ]">
                    <div class="flex-1 cursor-pointer" @click="handleAction('chat', chat.id)">
                        <h1 class="text-sm font-medium capitalize text-slate-700 dark:text-slate-200">
                            {{ chat.title }}
                        </h1>
                        <p class="text-xs text-slate-500 dark:text-slate-400">{{ chat.date }}</p>
                    </div>
                    <button @click.stop="handleAction('delete', chat.id)"
                        class="hidden rounded p-1 hover:bg-red-500 hover:text-white group-hover:block">
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


    </aside>
</template>