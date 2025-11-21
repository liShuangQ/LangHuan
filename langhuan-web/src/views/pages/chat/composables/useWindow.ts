/**
 * 聊天窗口管理功能组合式函数
 * 提供聊天窗口的创建、选择、删除、重命名等功能
 */
import { ref } from "vue";
import dayjs from "dayjs";
import * as api from "../api";
import type { ChatWindow } from "../types";

export function useWindow() {
    // 聊天窗口列表和当前窗口ID
    const chatList = ref<ChatWindow[]>([]);
    const currentWindowId = ref("");

    /**
     * 创建新的聊天窗口
     * @returns 新创建窗口的ID
     */
    const createWindow = async () => {
        // 创建新窗口对象
        const newWindow = {
            id: "new", // 临时ID，与后端约定
            title: "新窗口" + (chatList.value.length + 1),
            date: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            active: true,
        };

        // 保存到后端并获取真实ID
        const res = await api.saveChatMemory(newWindow.id, newWindow.title);
        newWindow.id = res.data;

        // 添加到列表并选为当前窗口
        chatList.value.push(newWindow);
        selectWindow(newWindow.id);

        return newWindow.id;
    };

    /**
     * 选择指定的聊天窗口
     * @param id 窗口ID
     */
    const selectWindow = (id: string) => {
        currentWindowId.value = id;
        // 更新所有窗口的激活状态
        chatList.value.forEach((chat) => {
            chat.active = chat.id === id;
        });
    };

    /**
     * 加载所有聊天窗口
     * @returns 第一个窗口的ID，如果没有窗口则返回空字符串
     */
    const loadWindows = async () => {
        const res = await api.getChatMemoryWindows();
        if (res.data.length !== 0) {
            // 将后端数据转换为窗口格式
            chatList.value = res.data.map((item: any) => ({
                id: item.conversationId,
                title: item.conversationName,
                date: item.createdTime,
                active: false,
            }));

            // 默认选择第一个窗口
            selectWindow(res.data[0].conversationId);
            return res.data[0].conversationId;
        }
        return "";
    };

    /**
     * 删除指定聊天窗口
     * @param id 窗口ID
     */
    const deleteWindow = async (id: string) => {
        // 从列表中移除
        chatList.value = chatList.value.filter((chat) => chat.id !== id);

        // 清除后端数据并重新加载列表
        await api.clearChatMemory(id);
        await loadWindows();
    };

    /**
     * 更新窗口名称
     * @param id 窗口ID
     * @param name 新的窗口名称
     */
    const updateWindowName = async (id: string, name: string) => {
        const window = chatList.value.find((w) => w.id === id);
        if (window) {
            // 更新本地数据
            window.title = name;
            // 同步到后端
            await api.setChatMemoryWindowsName(id, name);
        }
    };

    // 返回需要对外暴露的属性和方法
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
