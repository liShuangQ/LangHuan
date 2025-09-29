<template>
    <div class="ml-2 flex items-center">
        <el-dropdown>
            <el-icon size="20" class="el-icon--right ">
                <arrow-down/>
            </el-icon>
            <template #dropdown>
                <el-dropdown-menu>
                    <el-dropdown-item @click="menuHandle('now')">关闭其他标签页</el-dropdown-item>
                    <el-dropdown-item @click="menuHandle('left')">关闭左侧标签页</el-dropdown-item>
                    <el-dropdown-item @click="menuHandle('right')">关闭右侧标签页</el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
</template>

<script setup lang="ts">
import {ArrowDown} from '@element-plus/icons-vue'
import menu from '@/store/menu'
import router from "@/router";
const menuStore = menu()
const menuHandle = (t:"left" | "right" | "now"):void => {
    menuStore.handleHistoryMenu(router.currentRoute.value.path,t)
}
</script>

<style scoped>
.el-dropdown-link {
    color: var(--el-color-primary);
    display: flex;
    align-items: center;
}
</style>