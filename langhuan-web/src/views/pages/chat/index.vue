<script setup lang="ts">
import { onMounted } from 'vue'
import Sidebar from './components/sidebar.vue'
import PromptContainers from './components/prompt-containers.vue'
import { useChat } from './composables/useChat'
import { useWindow } from './composables/useWindow'
import user from "@/store/user";
const { messages, canSend, sendMessage, saveMemory, loadMessages, optimizePrompt, handleMessageAction } = useChat()
const { chatList, currentWindowId, createWindow, selectWindow, loadWindows, deleteWindow, updateWindowName } = useWindow()
const PromptContainersRef = ref<InstanceType<typeof PromptContainers> | null>(null)
const handleSidebarAction = async (type: string, payload?: any) => {
    console.log('handleSidebarAction', type, payload);

    if (type === 'new-chat') {
        const newId = createWindow()
        await loadMessages(newId)
    } else if (type === 'chat') {
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
    }
}

onMounted(async () => {
    const firstWindowId = await loadWindows()
    if (firstWindowId) {
        await loadMessages(firstWindowId)
    }
})
</script>

<template>
    <main class="flex w-full min-w-0 overflow-hidden">
        <Sidebar :chat-list="chatList" @action="handleSidebarAction" />
        <div class="min-w-0 flex-1">
            <PromptContainers ref="PromptContainersRef" :messages="messages" :can-send="canSend"
                @send-message="(msg: any) => sendMessage(currentWindowId, msg)" @action="handlePromptAction" />
        </div>
    </main>
</template>
