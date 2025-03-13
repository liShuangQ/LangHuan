<template>
    <div class="pr-4 h-full flex gap-2 justify-around items-center cursor-pointer">
        <el-tooltip
            content="刷新当前页面"
            effect="dark"
            placement="bottom"
        >
            <el-icon size="22" @click="emit('refreshPage')">
                <RefreshRight/>
            </el-icon>
        </el-tooltip>
        <el-tooltip
            content="使当前页面全屏显示"
            effect="dark"
            placement="bottom-end"
        >
            <el-icon size="20" @click="emit('pageFullScreen')">
                <FullScreen/>
            </el-icon>
        </el-tooltip>

        <el-dropdown class="ml-2">
            <el-avatar :size="30" shape="square" src="../../../static/favicon.ico"/>
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

const emit = defineEmits<{
    (event: 'refreshPage'): void
    (event: 'pageFullScreen'): void
}>()
const userStore = user()

</script>

<style scoped>

</style>