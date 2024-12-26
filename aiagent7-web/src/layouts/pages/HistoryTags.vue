<template>
    <el-tag
        v-for="item in menuStore.history"
        :key="item.path"
        class="mx-1 cursor-pointer hover:bg-blue-200"
        :closable="router.currentRoute.value.path !== item.path"
        @click="router.push({path: item.path});"
        @close="historyClose(item)"
        :effect="router.currentRoute.value.path === item.path ? 'dark':'plain'"
    >
<!--     closable   -->
        {{ item.title }}
    </el-tag>
</template>

<script lang="ts" setup>
import menu from "@/store/menu";
import {PagesMenu} from "@/router/menu";
import router from "@/router";

const menuStore = menu()
const historyClose = (path: PagesMenu): void => {
    menuStore.setHistoryDel(path)
}
</script>
