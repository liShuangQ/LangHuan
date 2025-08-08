import { ref } from "vue";
import dayjs from "dayjs";
import * as api from "../api";
import type { ChatWindow } from "../types";

export function useWindow() {
    const chatList = ref<ChatWindow[]>([]);
    const currentWindowId = ref("");

    const createWindow = async () => {
        const newWindow = {
            id: "new", // 和后端约定
            title: "新窗口" + (chatList.value.length + 1),
            date: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            active: true,
        };
        const res = await api.saveChatMemory(newWindow.id, newWindow.title);
        newWindow.id = res.data;
        chatList.value.push(newWindow);
        selectWindow(newWindow.id);
        return newWindow.id;
    };

    const selectWindow = (id: string) => {
        currentWindowId.value = id;
        chatList.value.forEach((chat) => {
            chat.active = chat.id === id;
        });
    };

    const loadWindows = async () => {
        const res = await api.getChatMemoryWindows();
        if (res.data.length !== 0) {
            chatList.value = res.data.map((item: any) => ({
                id: item.conversationId,
                title: item.conversationName,
                date: item.createdTime,
                active: false,
            }));
            selectWindow(res.data[0].conversationId);
            return res.data[0].conversationId;
        }
        return "";
    };

    const deleteWindow = async (id: string) => {
        chatList.value = chatList.value.filter((chat) => chat.id !== id);
        await api.clearChatMemory(id);
        await loadWindows();
    };

    const updateWindowName = async (id: string, name: string) => {
        const window = chatList.value.find((w) => w.id === id);
        if (window) {
            window.title = name;
            await api.setChatMemoryWindowsName(id, name);
        }
    };

    return {
        chatList,
        currentWindowId,
        createWindow,
        selectWindow,
        loadWindows,
        deleteWindow,
        updateWindowName,
    };
}
