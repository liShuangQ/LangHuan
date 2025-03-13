<template>
    <div class="ml-2">
        <el-breadcrumb class="flex items-center" separator="/">
            <el-breadcrumb-item class="cursor-pointer" @click="router.push(start)">
                <el-icon size="18" style="color:#000000">
                    <HomeFilled/>
                </el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item,index) in other" :key="index">{{ item.title }}</el-breadcrumb-item>
            <el-breadcrumb-item><span class="text-black font-bold">{{ now.title }}</span></el-breadcrumb-item>
        </el-breadcrumb>
    </div>
</template>

<script setup lang="ts">
import router from "@/router";
import menu from "@/store/menu";
import {PagesMenu} from "@/router/menu";

const menuStore = menu()
const flatMenu = toRaw(menuStore.flatMenu)
let start: string = '/'
let now = ref<PagesMenu>()
let other = ref<PagesMenu[]>()

watch(() => router.currentRoute.value.path, (newValue) => {
    now.value = flatMenu.find(e => {
        return e.path === newValue
    }) as PagesMenu
    other.value = now.value?.faPath.map(item => {
        return flatMenu.find(e => {
            return e.path === item
        })
    }) as PagesMenu[]
}, {immediate: true});

</script>

<style scoped>

</style>