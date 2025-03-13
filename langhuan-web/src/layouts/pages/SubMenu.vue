<template>
    <el-sub-menu :index="SubItem.path">
        <template #title>
            <el-icon v-if="SubItem.icon && SubItem.icon !== ''">
                <component :is="SubItem.icon"></component>
            </el-icon>
            <div v-else style="width: 24px"></div>
            <span>{{ SubItem.title }}</span>
        </template>
        <template v-for="item in SubItem.children" :key="item.path">
            <template v-if="!item.children || item.children === 0">
                <el-menu-item :index="item.path">
                    <el-icon v-if="item.icon && item.icon !== ''">
                        <component :is="item.icon"></component>
                    </el-icon>
                    <div v-else style="width: 24px"></div>
                    <span>{{ item.title }}</span>
                </el-menu-item>
            </template>
            <template v-else>
                <sub-menu :SubItem='item'></sub-menu>
            </template>
        </template>
    </el-sub-menu>
</template>
<script setup lang="ts">
const props = defineProps(["SubItem"]);
</script>
