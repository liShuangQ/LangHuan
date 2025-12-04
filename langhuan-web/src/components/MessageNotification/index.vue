<template>
    <div class="h-min " style="transform: translateY(3px);">
        <el-tooltip content="消息通知" effect="dark" placement="bottom">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0">
                <el-icon :size="iconSize" ref="messageButtonRef" :class="{ 'blink-icon': unreadCount > 0 }">
                    <ChatSquare class="text-slate-500" />
                </el-icon>
            </el-badge>
        </el-tooltip>

        <el-popover ref="messagePopoverRef" width="700" :placement="placement" :virtual-ref="messageButtonRef"
            trigger="click" virtual-triggering v-model:visible="messagePopoverVisible">
            <messageShow ref="messageShowRef" @unread-count-changed="handleUnreadCountChanged"></messageShow>
        </el-popover>
    </div>
</template>

<script lang="ts" setup>
import messageShow from "@/views/pages/systemManagement/messageShow/index.vue"
import { watch, onMounted, onUnmounted } from 'vue'
import { ChatSquare } from '@element-plus/icons-vue'
import { http } from '@/plugins/axios'
import user from '@/store/user'

// 定义props
interface Props {
    size?: number | string
    placement?: any
}

const props = withDefaults(defineProps<Props>(), {
    size: 22,
    placement: 'bottom-end'
})

// 计算图标大小
const iconSize = computed(() => props.size)

const messageButtonRef = ref()
const messagePopoverRef = ref()
const messagePopoverVisible = ref(false)
const messageShowRef = ref()
const unreadCount = ref(0)

// 获取未读消息数量
const getUnreadCount = async () => {
    try {
        const currentUserId = user().info.user.username
        const response = await http.request<any>({
            url: '/notifications/getPersonalUnreadCount',
            method: 'post',
            q_contentType: 'form',
            data: {
                userId: currentUserId
            }
        })
        unreadCount.value = response.data || 0
    } catch (error) {
        console.error('获取未读消息数量失败:', error)
    }
}

// 组件挂载时获取未读消息数量
onMounted(() => {
    getUnreadCount()
})

// 处理未读消息数量变化
const handleUnreadCountChanged = () => {
    getUnreadCount()
}

// 监听popover显示状态，当显示时自动查询消息列表
watch(messagePopoverVisible, (newVal) => {
    if (newVal && messageShowRef.value) {
        // 当popover显示时，调用messageShow组件的refreshPage方法查询消息列表
        messageShowRef.value.refreshPage()
        // 刷新未读消息数量
        getUnreadCount()
    }
})


</script>

<style scoped>
.el-icon {
    cursor: pointer;
}

/* 闪烁动画 */
.blink-icon {
    animation: blink 1.5s infinite;
}

@keyframes blink {

    0%,
    50% {
        opacity: 1;
    }

    51%,
    100% {
        opacity: 0.3;
    }
}
</style>