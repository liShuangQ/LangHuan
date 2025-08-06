<script lang="ts">
export default {
    auto: true,
};
</script>
<script setup lang="ts">
import { onMounted } from "vue";
import Sidebar from "./components/sidebar.vue";
import SettingsSidebar from "./components/settings-sidebar.vue";
import PromptContainers from "./components/prompt-containers.vue";
import UpdateTip from "./components/updateTip.vue";
import { useChat } from "./composables/useChat";
import { useWindow } from "./composables/useWindow";
import { useSettings } from "./composables/useSettings";
import {
    expertPrompt,
    observerPrompt,
    questionGeneratorPrompt,
    summarizePrompt,
} from "./config";
import user from "@/store/user";
import * as api from "./api";

const {
    messages,
    canSend,
    sendMessage,
    saveMemory,
    loadMessages,
    optimizePrompt,
    handleMessageAction,
    documentRank,
} = useChat();
const {
    chatList,
    currentWindowId,
    createWindow,
    selectWindow,
    loadWindows,
    deleteWindow,
    updateWindowName,
} = useWindow();
const {
    showSettings,
    settings,
    availableModels,
    ragGroups,
    toggleSettings,
    getChatParams,
} = useSettings();
const PromptContainersRef = ref<InstanceType<typeof PromptContainers> | null>(
    null
);
const router = useRouter();
const nowIsChat = router.currentRoute.value.path === "/chat";

const handleSidebarAction = async (type: string, payload?: any) => {
    if (type === "new-chat") {
        const newId = createWindow();
        await loadMessages(newId);
    } else if (type === "barChat") {
        return;
    } else if (type === "chat" && payload) {
        selectWindow(payload);
        await loadMessages(payload);
    } else if (type === "delete") {
        await deleteWindow(payload);
        const oneId = chatList.value[0]?.id;
        if (oneId) {
            selectWindow(oneId);
            await loadMessages(oneId);
        }
    } else if (type === "logout") {
        user().userLogOut();
    } else if (type === "settings") {
        toggleSettings();
    }
};

const handlePromptAction = async (type: string, payload?: any) => {
    if (type === "saveMemory") {
        await saveMemory(currentWindowId.value);
    } else if (type === "optimizePrompt") {
        if (PromptContainersRef.value) {
            PromptContainersRef.value.setMessageInput(
                await optimizePrompt(
                    PromptContainersRef.value.getMessageInput()
                )
            );
        }
    } else if (["like", "dislike", "copy"].includes(type)) {
        await handleMessageAction(type, payload, settings.value);
    } else if (type === "documentRank") {
        await documentRank(payload);
    }
};

// XXX 后续可优化记忆压缩，超出记忆的时候，总是留下总结者的信息
const sendMessageExpertMode = async (windowId: string, message: string) => {
    if (settings.value.expertFileGroups.length < 2) {
        ElMessage.warning("请选择至少两个文件组");
        return;
    }
    let expertFileGroups: any = JSON.parse(
        JSON.stringify(toRaw(settings.value.expertFileGroups))
    );
    expertFileGroups.push({
        id: "observer",
        name: "观察者",
    });
    // 优化提示词，后面在二轮后会用其它问题替换这个提示词
    let newMessage = message;
    for (let i = 0; i < settings.value.expertConversationRounds; i++) {
        if (settings.value.expertConversationRounds === i + 1) {
            // 最后一轮不需要观察者
            expertFileGroups = expertFileGroups.filter(
                (item: any) => item.id !== "observer"
            );
        }
        // 一轮信息
        for (let index = 0; index < expertFileGroups.length; index++) {
            await sendMessage(windowId, `第${i + 1}轮问题：${newMessage}`, {
                ...getChatParams.value,
                groupId: expertFileGroups[index].id,
                isRag: expertFileGroups[index].id === "observer" ? false : true,
                p:
                    expertFileGroups[index].id === "observer"
                        ? observerPrompt.replaceAll(
                              "{currentRound}",
                              String(i + 1)
                          )
                        : expertPrompt.replaceAll(
                              "{currentRound}",
                              String(i + 1)
                          ).replaceAll(
                              "{fileGroupName}",
                              expertFileGroups[index].name
                          ) +
                          "\n" +
                          getChatParams.value.p,
                fileGroupName: expertFileGroups[index].name,
                showUserMessage: index === 0,
            });
        }

        // 不是最后一轮，需要生成下一轮的问题
        if (settings.value.expertConversationRounds !== i + 1) {
            const res = await api.sendEasyChatMessage({
                p: questionGeneratorPrompt
                    .replaceAll("{currentRound}", String(i + 1))
                    .replaceAll("{nextRound}", String(i + 2)),
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

    // 完成后总结
    await sendMessage(windowId, `总结问题`, {
        ...getChatParams.value,
        groupId: "",
        isRag: false,
        p: summarizePrompt.replaceAll(
            "{totalRounds}",
            String(settings.value.expertConversationRounds)
        ),
        fileGroupName: "总结者",
        showUserMessage: true,
    });
};

const handleSendMessage = (windowId: string, message: string) => {
    if (settings.value.isExpertMode) {
        sendMessageExpertMode(windowId, message);
    } else {
        sendMessage(windowId, message, {
            ...getChatParams.value,
            fileGroupName: settings.value.ragGroup?.name,
        });
    }
};

onMounted(async () => {
    const firstWindowId = await loadWindows();
    if (firstWindowId) {
        await loadMessages(firstWindowId);
    }
});
</script>

<template>
    <main
        :class="
            nowIsChat
                ? ['flex', 'h-screen', 'w-full', 'min-w-0', 'overflow-hidden']
                : ['flex', 'h-full', 'w-full', 'min-w-0', 'overflow-hidden']
        "
    >
        <Sidebar :chat-list="chatList" @action="handleSidebarAction" />
        <div class="min-w-0 flex-1 h-full">
            <PromptContainers
                ref="PromptContainersRef"
                :messages="messages"
                :can-send="canSend"
                :has-windows="chatList.length > 0"
                @send-message="(msg: any) => handleSendMessage(currentWindowId, msg)"
                @action="handlePromptAction"
            />
        </div>
        <SettingsSidebar
            v-if="showSettings"
            v-model="settings"
            :available-models="availableModels"
            :rag-groups="ragGroups"
            @close="toggleSettings"
        />
        <UpdateTip />
    </main>
</template>
