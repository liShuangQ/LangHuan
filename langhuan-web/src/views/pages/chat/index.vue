<script setup lang="ts">
import { onMounted } from 'vue'
import Sidebar from './components/sidebar.vue'
import SettingsSidebar from './components/settings-sidebar.vue'
import PromptContainers from './components/prompt-containers.vue'
import { useChat } from './composables/useChat'
import { useWindow } from './composables/useWindow'
import { useSettings } from './composables/useSettings'
import user from "@/store/user";
const { messages, canSend, sendMessage, saveMemory, loadMessages, optimizePrompt, handleMessageAction, documentRank } = useChat()
const { chatList, currentWindowId, createWindow, selectWindow, loadWindows, deleteWindow, updateWindowName } = useWindow()
const { showSettings, settings, availableModels, ragGroups, toggleSettings, getChatParams } = useSettings()
const PromptContainersRef = ref<InstanceType<typeof PromptContainers> | null>(null)
import { useRoute } from "vue-router";
const router = useRouter();
const nowIsChat = router.currentRoute.value.path.includes('chat')
const handleSidebarAction = async (type: string, payload?: any) => {
    console.log('handleSidebarAction', type, payload);

    if (type === 'new-chat') {
        const newId = createWindow()
        await loadMessages(newId)
    } else if (type === 'barChat') {
        return;
    } else if (type === 'chat' && payload) {
        selectWindow(payload)
        await loadMessages(payload)
    } else if (type === 'delete') {
        await deleteWindow(payload)
        const oneId = chatList.value[0]?.id
        if (oneId) {
            selectWindow(oneId)
            await loadMessages(oneId)
        }
    } else if (type === 'logout') {
        user().userLogOut()
    } else if (type === 'settings') {
        toggleSettings()
    }
}

const handlePromptAction = async (type: string, payload?: any) => {
    console.log('handlePromptAction', type, payload);

    if (type === 'saveMemory') {
        await saveMemory(currentWindowId.value)
    } else if (type === 'optimizePrompt') {
        if (PromptContainersRef.value) {
            PromptContainersRef.value.setMessageInput(await optimizePrompt(PromptContainersRef.value.getMessageInput()))
        }
    } else if (['like', 'dislike', 'copy'].includes(type)) {
        await handleMessageAction(type, payload)
    } else if (type === 'documentRank') {
        await documentRank(payload)
    }
}

const handleSendMessage = (windowId: string, message: string) => {
    sendMessage(windowId, message, getChatParams.value)
}

onMounted(async () => {
    const firstWindowId = await loadWindows()
    if (firstWindowId) {
        await loadMessages(firstWindowId)
    }
})
</script>

<template>
    <main
        :class="nowIsChat ? ['flex', 'h-screen', 'w-full', 'min-w-0', 'overflow-hidden'] : ['flex', 'h-full', 'w-full', 'min-w-0', 'overflow-hidden']">
        <Sidebar :chat-list="chatList" @action="handleSidebarAction" />
        <div class="min-w-0 flex-1 h-full">
            <PromptContainers ref="PromptContainersRef" :messages="messages" :can-send="canSend"
                @send-message="(msg: any) => handleSendMessage(currentWindowId, msg)" @action="handlePromptAction" />
        </div>
        <SettingsSidebar v-if="showSettings" v-model="settings" :available-models="availableModels"
            :rag-groups="ragGroups" @close="toggleSettings" />
    </main>
</template>
