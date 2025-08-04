<script setup lang="ts">
import { computed } from 'vue'

interface Props {
    loading: boolean
}

defineProps<Props>()
const emit = defineEmits<{
    refresh: []
}>()

// 基础项目名称
const BASE_PROJECT_NAME = computed(() => {
    return process.env.BASE_PROJECT_NAME as string
})
</script>

<template>
    <div class="flex justify-between items-center mb-6">
        <div>
            <h1 class="text-3xl font-bold text-gray-800 mb-2">{{ BASE_PROJECT_NAME }}系统仪表盘</h1>
        </div>
        <el-button :loading="loading" @click="$emit('refresh')" class="refresh-btn">
            <el-icon>
                <Refresh />
            </el-icon>
        </el-button>
    </div>
</template>

<style scoped>
.refresh-btn {
    transition: all 0.3s ease;
}

.refresh-btn:hover {
    transform: scale(1.05);
}
</style>