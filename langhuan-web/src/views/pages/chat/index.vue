<!-- 聊天主页面组件 -->
<script lang="ts">
export default {
    auto: true,
};
</script>
<script setup lang="ts">
// 导入Vue核心功能
import { onMounted, ref } from "vue";
// 导入Element Plus组件
import { ElLoading, ElMessageBox, ElMessage } from "element-plus";
// 导入页面子组件
import Sidebar from "./components/sidebar.vue";
import SettingsSidebar from "./components/settings-sidebar.vue";
import PromptContainers from "./components/prompt-containers.vue";
import UpdateTip from "./components/updateTip.vue";
// 导入工具函数
import { exportElementToHtml, generateExportFilename } from "@/utils/exportHtml";
// 导入组合式函数
import { useChat } from "./composables/useChat";
import { useWindow } from "./composables/useWindow";
import { useSettings } from "./composables/useSettings";
// 导入用户状态管理和API
import user from "@/store/user";
// 导入类型定义
import { ChatSeedEmitMessageData, ChatWindow, } from "./types";

// 使用聊天相关功能的组合式函数
const {
    messages,
    canSend,
    sendMessage,
    loadMessages,
    optimizePrompt,
    handleMessageAction,
    documentRank,
    sendMessageExpertMode
} = useChat();
// 使用窗口管理相关功能的组合式函数
const {
    chatWindowList,
    currentWindowId,
    createWindow,
    selectWindow,
    loadWindows,
    deleteWindow,
    updateWindowName,
} = useWindow();
// 使用设置相关功能的组合式函数
const {
    showSettings,
    settings,
    availableModels,
    ragGroups,
    toggleSettings,
    getChatParams,
    setChatParams,
    updateSettingsConfig
} = useSettings();
// user pinia
const userStore = user();
// 提示词容器的引用
const PromptContainersRef = ref<InstanceType<typeof PromptContainers> | null>(
    null
);
// 获取路由实例
const router = useRouter();
// 判断当前是否在聊天页面
const nowIsChat = router.currentRoute.value.path === "/chat";
// 切换聊天窗口时的操作
const switchChatWindow = async (toWindowId: string) => {
    const windowInfo = chatWindowList.value.find((e: ChatWindow) => {
        return e.id === toWindowId
    }) as ChatWindow;
    selectWindow(toWindowId);
    setChatParams(windowInfo.settingConfig);
    await loadMessages(toWindowId);
};

/**
 * 处理侧边栏操作事件
 * @param type 操作类型
 * @param payload 操作参数
 */
const handleSidebarAction = async (type: string, payload?: any) => {
    if (type === "new-chat") {
        // 创建新聊天窗口
        // 配置的默认文件组，${username}会被替换为用户名
        const GROUP_BASE_NAMES = process.env.GROUP_BASE_NAMES?.replaceAll(
            "${username}",
            userStore.info.user.username
        )?.split(",") as string[];
        const allDefault = GROUP_BASE_NAMES
            .map((e: string) => {
                return (
                    ragGroups.value.find((g) => g.name === e)
                );
            })
            .filter((e) => !!e);
        const newId = await createWindow(
            {
                id: allDefault[0]?.id || "",
                name: allDefault[0]?.name || ""
            }
        );
        await switchChatWindow(newId);
        // 这里在新创建的时候是需要手动设置的，这里就像是提前把东西都准备好了。
        // 而在每次修改更新的时候，是由内而外触发的（此页面的“updateSettingsConfig”），重新赋值。
        await updateSettingsConfig(newId, toRaw(settings.value));
        await loadMessages(newId);
    } else if (type === "barChat") {
        // 处理侧边栏聊天（暂时不做处理）
        return;
    } else if (type === "chat" && payload) {
        await switchChatWindow(payload);
    } else if (type === "delete") {
        // 删除聊天窗口
        await deleteWindow(payload);
        const oneId = chatWindowList.value[0]?.id;
        if (oneId) {
            await switchChatWindow(oneId);
        }
    } else if (type === "logout") {
        // 用户登出
        userStore.userLogOut();
    } else if (type === "settings") {
        // 切换设置面板显示状态
        toggleSettings();
    } else if (type === "editend") {
        // 更新窗口名称
        updateWindowName(payload.id, payload.title);
    }
};

/**
 * 处理提示词相关操作事件
 * @param type 操作类型
 * @param payload 操作参数
 */
const handlePromptAction = async (type: string, payload?: any) => {
    if (type === "optimizePrompt") {
        // 优化用户输入的提示词
        if (PromptContainersRef.value) {
            PromptContainersRef.value.setMessageInput(
                await optimizePrompt(
                    PromptContainersRef.value.getMessageInput()
                )
            );
        }
    } else if (type === "exportChatMessage") {
        // 导出聊天记录
        const loading = ElLoading.service({
            lock: true,
            text: '正在导出聊天记录...',
            background: 'rgba(0, 0, 0, 0.7)',
        });

        try {
            if (payload && payload.value) {
                // 弹出输入框让用户输入文件名
                const { value: fileName } = await ElMessageBox.prompt('请输入导出文件名', '导出聊天记录', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    inputPlaceholder: '例如：我的聊天记录',
                    inputValidator: (value: string) => {
                        if (!value || !value.trim()) {
                            return '文件名不能为空';
                        }
                        return true;
                    },
                });

                if (fileName) {
                    const cleanFileName = fileName.trim();
                    const filename = generateExportFilename(cleanFileName);
                    await exportElementToHtml(payload.value, filename, cleanFileName);
                    ElMessage.success('聊天记录导出成功');
                }
            } else {
                ElMessage.warning('无法获取聊天内容，请稍后重试');
            }
        } catch (error: any) {
            if (error === 'cancel') {
                // 用户取消了输入
                loading.close();
                return;
            }
            ElMessage.error('导出聊天记录失败');
        } finally {
            loading.close();
        }

    } else if (["like", "dislike", "copy"].includes(type)) {
        // 处理消息反馈操作（点赞、点踩、复制）
        await handleMessageAction(type, payload, settings.value);
    } else if (type === "documentRank") {
        // 处理文档相关性评分
        await documentRank(payload);
    }
};

/**
 * 处理发送消息事件
 * @param windowId 窗口ID
 * @param messageData 消息数据
 */
const handleSendMessage = (windowId: string, messageData: ChatSeedEmitMessageData) => {
    if (settings.value.isExpertMode) {
        // 专家模式发送消息
        sendMessageExpertMode(toRaw(settings.value), toRaw(getChatParams.value), windowId, messageData.userMessage);
    } else {
        // 普通模式发送消息
        sendMessage(windowId, {
            ...getChatParams.value,
            ...messageData,
            fileGroupName: settings.value.ragGroup?.name,
        });
    }
};

/**
 * 处理设置侧边栏操作事件
 */
const handleSettingsAction = async (type: string, payload?: any) => {
    if (type === "updateSettingsConfig") {
        // 后端更新-设置配置
        await updateSettingsConfig(currentWindowId.value, payload);
        await loadWindows();
    }
}

// 组件挂载时的初始化操作
onMounted(async () => {
    // 加载所有聊天窗口
    const firstWindowId = await loadWindows();
    if (firstWindowId) {
        await switchChatWindow(firstWindowId);
    }
});
</script>

<template>
    <!-- 聊天主界面布局 -->
    <main :class="nowIsChat
        ? ['flex', 'h-screen', 'w-full', 'min-w-0', 'overflow-hidden']
        : ['flex', 'h-full', 'w-full', 'min-w-0', 'overflow-hidden']
        ">
        <!-- 侧边栏：显示聊天列表 -->
        <Sidebar :chatWindowList="chatWindowList" @action="handleSidebarAction" />

        <!-- 主聊天区域 -->
        <div class="min-w-0 flex-1 h-full">
            <PromptContainers ref="PromptContainersRef" :messages="messages" :can-send="canSend"
                :has-windows="chatWindowList.length > 0"
                @send-message="(msg: any) => handleSendMessage(currentWindowId, msg)" @action="handlePromptAction" />
        </div>

        <!-- 设置侧边栏 -->
        <SettingsSidebar v-if="showSettings" v-model="settings" :available-models="availableModels"
            :rag-groups="ragGroups" @close="toggleSettings" @action="handleSettingsAction" />

        <!-- 更新提示组件 -->
        <UpdateTip />
    </main>
</template>
