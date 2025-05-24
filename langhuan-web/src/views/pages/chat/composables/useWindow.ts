import { ref } from "vue";
import * as api from "../api";
import { generateUUID } from "@/utils/uuid";
import type { ChatWindow } from "../types";
import { el } from "element-plus/es/locale";

export function useWindow() {
    const chatList = ref<ChatWindow[]>([]);
    const currentWindowId = ref("");

    const createWindow = () => {
        const date = new Date().toISOString();
        const newWindow = {
            id: generateUUID(),
            title: "新窗口",
            date: date.split("T")[0] + " " + date.split("T")[1].split("+")[0],
            active: true,
        };
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
                date:
                    item.createdTime.split("T")[0] +
                    " " +
                    item.createdTime.split("T")[1].split("+")[0],
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
