<!-- 聊天主页面组件 -->
<script lang="ts">
export default {
    auto: true,
};
</script>
<script setup lang="ts">
// 导入Vue核心功能
import {onMounted, ref} from "vue";
// 导入Element Plus组件
import {ElLoading, ElMessageBox, ElMessage} from "element-plus";
// 导入页面子组件
import Sidebar from "./components/sidebar.vue";
import SettingsSidebar from "./components/settings-sidebar.vue";
import PromptContainers from "./components/prompt-containers.vue";
import UpdateTip from "./components/updateTip.vue";
// 导入工具函数
import {exportElementToHtml, generateExportFilename} from "@/utils/exportHtml";
// 导入组合式函数
import {useChat} from "./composables/useChat";
import {useWindow} from "./composables/useWindow";
import {useSettings} from "./composables/useSettings";
// 导入配置项和提示词模板
import {
    expertPrompt,
    observerPrompt,
    questionGeneratorPrompt,
    summarizePrompt,
} from "./config";
// 导入用户状态管理和API
import user from "@/store/user";
import * as api from "./api";
// 导入类型定义
import {ChatSeedEmitMessageData, ChatSendParam} from "./types";

// 使用聊天相关功能的组合式函数
const {
    messages,
    canSend,
    sendMessage,
    loadMessages,
    optimizePrompt,
    handleMessageAction,
    documentRank,
} = useChat();
// 使用窗口管理相关功能的组合式函数
const {
    chatList,
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
} = useSettings();
// 提示词容器的引用
const PromptContainersRef = ref<InstanceType<typeof PromptContainers> | null>(
    null
);
// 获取路由实例
const router = useRouter();
// 判断当前是否在聊天页面
const nowIsChat = router.currentRoute.value.path === "/chat";

/**
 * 处理侧边栏操作事件
 * @param type 操作类型
 * @param payload 操作参数
 */
const handleSidebarAction = async (type: string, payload?: any) => {
    if (type === "new-chat") {
        // 创建新聊天窗口
        const newId = await createWindow();
        await loadMessages(newId);
    } else if (type === "barChat") {
        // 处理侧边栏聊天（暂时不做处理）
        return;
    } else if (type === "chat" && payload) {
        // 选择指定聊天窗口
        selectWindow(payload);
        await loadMessages(payload);
    } else if (type === "delete") {
        // 删除聊天窗口
        await deleteWindow(payload);
        const oneId = chatList.value[0]?.id;
        if (oneId) {
            // 切换到第一个可用的聊天窗口
            selectWindow(oneId);
            await loadMessages(oneId);
        }
    } else if (type === "logout") {
        // 用户登出
        user().userLogOut();
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
                const {value: fileName} = await ElMessageBox.prompt('请输入导出文件名', '导出聊天记录', {
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
            console.error('导出聊天记录失败:', error);
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
 * 专家模式发送消息 - 支持多轮对话和多文件组专家咨询
 * TODO: 后续可优化记忆压缩，超出记忆的时候，总是留下总结者的信息
 * @param windowId 窗口ID
 * @param message 用户消息
 */
const sendMessageExpertMode = async (windowId: string, message: string) => {
    // 验证是否选择了至少两个文件组
    if (settings.value.expertFileGroups.length < 2) {
        ElMessage.warning("请选择至少两个文件组");
        return;
    }

    // 复制专家文件组数据并添加观察者角色
    let expertFileGroups: any = JSON.parse(
        JSON.stringify(toRaw(settings.value.expertFileGroups))
    );
    expertFileGroups.push({
        id: "observer",
        name: "观察者",
    });

    // 优化提示词，后面在二轮后会用其它问题替换这个提示词
    let newMessage = message;

    // 按配置的轮数进行多轮专家对话
    for (let i = 0; i < settings.value.expertConversationRounds; i++) {
        // 最后一轮不需要观察者
        if (settings.value.expertConversationRounds === i + 1) {
            expertFileGroups = expertFileGroups.filter(
                (item: any) => item.id !== "observer"
            );
        }

        // 当前轮次的专家发言
        for (let index = 0; index < expertFileGroups.length; index++) {
            await sendMessage(windowId, {
                ...getChatParams.value,
                ragGroupId: expertFileGroups[index].id,
                isRag: expertFileGroups[index].id === "observer" ? false : true,
                prompt:
                    expertFileGroups[index].id === "observer"
                        ? observerPrompt.replaceAll(
                            "{currentRound}",
                            String(i + 1)
                        )
                        : expertPrompt
                            .replaceAll("{currentRound}", String(i + 1))
                            .replaceAll(
                                "{fileGroupName}",
                                expertFileGroups[index].name
                            ) +
                        "\n" +
                        getChatParams.value.prompt,
                fileGroupName: expertFileGroups[index].name,
                showUserMessage: index === 0,
                userMessage: `第${i + 1}轮问题：${newMessage}`,
            });
        }

        // 不是最后一轮，需要生成下一轮的问题
        if (settings.value.expertConversationRounds !== i + 1) {
            const res = await api.sendEasyChatMessage({
                p: questionGeneratorPrompt
                    .replaceAll("{currentRound}", String(i + 1))
                    .replaceAll("{nextRound}", String(i + 2))
                    .replaceAll("{originalQuestion}", message),
                q: messages.value
                    .filter(
                        (item: any) =>
                            item.sender === "assistant" &&
                            item.content !== "正在思考中..."
                    )
                    .slice(-expertFileGroups.length)
                    .map((item: any) => item.content)
                    .join("\n"),
                modelName: getChatParams.value.modelName,
            });
            newMessage = res.data.chat;
        }
    }

    // 完成后进行总结
    await sendMessage(windowId, {
        ...getChatParams.value,
        ragGroupId: "",
        isRag: false,
        prompt: summarizePrompt.replaceAll(
            "{totalRounds}",
            String(settings.value.expertConversationRounds)
        ),
        fileGroupName: "总结者",
        showUserMessage: true,
        userMessage: `总结问题`,
    });
};

/**
 * 处理发送消息事件
 * @param windowId 窗口ID
 * @param messageData 消息数据
 */
const handleSendMessage = (windowId: string, messageData: ChatSeedEmitMessageData) => {
    if (settings.value.isExpertMode) {
        // 专家模式发送消息
        sendMessageExpertMode(windowId, messageData.userMessage);
    } else {
        // 普通模式发送消息
        sendMessage(windowId, {
            ...getChatParams.value,
            ...messageData,
            fileGroupName: settings.value.ragGroup?.name,
        });
    }
};

// 组件挂载时的初始化操作
onMounted(async () => {
    // 加载所有聊天窗口
    const firstWindowId = await loadWindows();
    if (firstWindowId) {
        // 加载第一个窗口的消息历史
        await loadMessages(firstWindowId);
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
        <Sidebar :chat-list="chatList" @action="handleSidebarAction"/>

        <!-- 主聊天区域 -->
        <div class="min-w-0 flex-1 h-full">
            <PromptContainers ref="PromptContainersRef" :messages="messages" :can-send="canSend"
                              :has-windows="chatList.length > 0"
                              @send-message="(msg: any) => handleSendMessage(currentWindowId, msg)"
                              @action="handlePromptAction"/>
        </div>

        <!-- 设置侧边栏 -->
        <SettingsSidebar v-if="showSettings" v-model="settings" :available-models="availableModels"
                         :rag-groups="ragGroups" @close="toggleSettings"/>

        <!-- 更新提示组件 -->
        <UpdateTip/>
    </main>
</template>
