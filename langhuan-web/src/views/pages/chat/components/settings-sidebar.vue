<script setup lang="ts">
import { computed } from 'vue'
import { Close } from '@element-plus/icons-vue'
import type { ChatSettings, RagGroup } from '../types'

defineOptions({
    name: 'SettingsSidebar'
})

const props = defineProps<{
    modelValue: ChatSettings,  // 改用 v-model 的标准命名
    availableModels: { label: string; value: string }[]
    ragGroups: RagGroup[]
}>()

const emit = defineEmits<{
    (e: 'update:modelValue', value: ChatSettings): void
    (e: 'close'): void
}>()

// 使用计算属性处理双向绑定
const modelName = computed({
    get: () => props.modelValue.modelName,
    set: (value) => emit('update:modelValue', { ...props.modelValue, modelName: value })
})

const promptTemplate = computed({
    get: () => props.modelValue.promptTemplate,
    set: (value) => emit('update:modelValue', { ...props.modelValue, promptTemplate: value })
})

// 修改计算属性处理方式
const ragGroup = computed({
    get: () => props.modelValue.ragGroup?.id || '',
    set: (value) => {
        const selectedGroup = props.ragGroups.find(g => g.id === value)
        const newSettings = { ...props.modelValue, ragGroup: selectedGroup || null }
        // 当选择"无"时，自动关闭ReRank
        if (!value) {
            newSettings.isReRank = false
        }
        emit('update:modelValue', newSettings)
    }
})

const isReRank = computed({
    get: () => props.modelValue.isReRank,
    set: (value) => emit('update:modelValue', { ...props.modelValue, isReRank: value })
})

// 计算ReRank开关是否应该被禁用
const isReRankDisabled = computed(() => !props.modelValue.ragGroup?.id)
</script>

<template>
    <aside class="flex flex-row-reverse">
        <div class="relative h-screen w-60 overflow-y-auto border-l border-slate-300 bg-slate-50 py-8 dark:border-slate-700 dark:bg-slate-900 sm:w-64">
            <div class="mb-4 flex items-center justify-between px-4">
                <h2 class="text-lg font-medium text-slate-800 dark:text-slate-200">设置</h2>
                <el-button link @click="$emit('close')" :icon="Close" />
            </div>

            <div class="px-4 space-y-6">
                <!-- 模型选择 -->
                <div>
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1">使用的模型</label>
                    <el-select
                        v-model="modelName"
                        size="small"
                        class="w-full"
                    >
                        <el-option
                            v-for="model in availableModels"
                            :key="model.value"
                            :label="model.label"
                            :value="model.value"
                        />
                    </el-select>
                </div>

                <!-- 提示词 -->
                <div>
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1">提示词</label>
                    <el-input
                        v-model="promptTemplate"
                        type="textarea"
                        size="small"
                        :rows="3"
                        placeholder="输入提示词..."
                    />
                </div>

                <!-- RAG组选择 -->
                <div>
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1">RAG文档组</label>
                    <el-select
                        v-model="ragGroup"
                        size="small"
                        class="w-full"
                        placeholder="选择RAG文档组"
                    >
                        <el-option
                            v-for="group in ragGroups"
                            :key="group.id"
                            :label="group.name"
                            :value="group.id"
                        />
                    </el-select>
                </div>

                <!-- ReRank模型开关 -->
                <div>
                    <label class="block text-sm font-medium text-slate-700 dark:text-slate-300 pb-1">启用ReRank模型</label>
                    <el-switch
                        v-model="isReRank"
                        size="small"
                        active-text="开启"
                        inactive-text="关闭"
                        :disabled="isReRankDisabled"
                    />
                    <div v-if="isReRankDisabled" class="text-xs text-slate-500 dark:text-slate-400 mt-1">
                        需要选择RAG文档组才能启用ReRank模型
                    </div>
                </div>
            </div>
        </div>
    </aside>
</template>
