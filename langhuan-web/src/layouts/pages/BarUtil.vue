<template>
    <div class="pr-4 h-full flex gap-2 justify-around items-center cursor-pointer">

        <el-tooltip content="消息通知" effect="dark" placement="bottom">
            <el-icon size="22" ref="messageButtonRef">
                <ChatSquare />
            </el-icon>
        </el-tooltip>

        <el-popover ref="messagePopoverRef" width="700" :virtual-ref="messageButtonRef" trigger="click"
            virtual-triggering v-model:visible="messagePopoverVisible">
            <messageShow ref="messageShowRef"></messageShow>
        </el-popover>

        <el-tooltip content="刷新当前页面" effect="dark" placement="bottom">
            <el-icon size="22" @click="emit('refreshPage')">
                <RefreshRight />
            </el-icon>
        </el-tooltip>
        <el-tooltip content="使当前页面全屏显示" effect="dark" placement="bottom-end">
            <el-icon size="20" @click="emit('pageFullScreen')">
                <FullScreen />
            </el-icon>
        </el-tooltip>

        <el-dropdown class="ml-2">
            <el-avatar :size="30" shape="square" src="../../../static/favicon.ico" />
            <template #dropdown>
                <el-dropdown-menu>
                    <el-dropdown-item @click="userStore.userLogOut()">登出</el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
</template>
<script lang="ts" setup>
import user from "@/store/user";
import messageShow from "@/views/pages/systemManagement/messageShow/index.vue"
import { watch } from 'vue'

const emit = defineEmits<{
    (event: 'refreshPage'): void
    (event: 'pageFullScreen'): void
}>()
const userStore = user()


const messageButtonRef = ref()
const messagePopoverRef = ref()
const messagePopoverVisible = ref(true)
const messageShowRef = ref()

// 监听popover显示状态，当显示时自动查询消息列表
watch(messagePopoverVisible, (newVal) => {
    if (newVal && messageShowRef.value) {
        // 当popover显示时，调用messageShow组件的refreshPage方法查询消息列表
        messageShowRef.value.refreshPage()
    }
})

</script>

<style scoped></style>