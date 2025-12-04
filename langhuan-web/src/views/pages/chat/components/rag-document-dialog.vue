<script setup lang="ts">
import { Top, Bottom } from '@element-plus/icons-vue'

const props = defineProps<{
    modelValue: boolean
    documents: any[]
}>()

const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'rank', type: 'good' | 'bad', document: any): void
}>()

const handleRank = (type: 'good' | 'bad', document: any) => {
    emit('rank', type, document)
}
</script>

<template>
    <el-dialog
        :model-value="modelValue"
        @update:model-value="emit('update:modelValue', $event)"
        title="引用的知识库"
        width="900"
        :show-close="true"
        :close-on-click-modal="false"
    >
        <div class="h-[50vh] overflow-y-auto pr-2 space-y-4">
            <div v-for="(item, index) in documents" :key="item.id"
                 class="document-item bg-gray-50 rounded-lg p-4 border border-gray-200 hover:border-blue-300 transition-colors">

                <!-- 文档内容 -->
                <div class="mb-3">
                    <div class="flex items-start gap-2 mb-2">
                        <span class="inline-flex items-center justify-center w-6 h-6 text-xs font-medium text-white bg-blue-500 rounded-full">
                            {{ index + 1 }}
                        </span>
                        <span class="text-sm text-gray-500">
                            文档 ID: {{ item.id }}
                        </span>
                    </div>
                    <p class="text-gray-800 leading-relaxed text-base">
                        {{ item.text }}
                    </p>
                </div>

                <!-- 元数据信息 -->
                <div class="space-y-2">
                    <!-- 第一行：基础信息 -->
                    <div class="flex flex-wrap items-center gap-2">
                        <el-tag type="primary" effect="light" size="small">
                            <span class="font-medium">来源文件：</span>{{ item.metadata.filename }}
                        </el-tag>
                    </div>

                    <!-- 第二行：详细分数 -->
                    <div class="flex flex-wrap items-center gap-2">
                        <el-tag type="danger" effect="light" size="small">
                            <span class="font-medium">加权得分：</span>{{ item.metadata.weightedScore }}
                        </el-tag>
                        <el-tag type="warning" effect="light" size="small">
                            <span class="font-medium">向量得分：</span>{{ item.metadata.normalizedSpringAiScore }}
                        </el-tag>
                        <el-tag type="light" effect="light" size="small">
                            <span class="font-medium">关键字得分：</span>{{ item.metadata.normalizedBm25Score }}
                        </el-tag>
                        <el-tag effect="info" size="small">
                            <span class="font-medium">手工排名：</span>{{ item.metadata.normalizedRankScore }}
                        </el-tag>
                    </div>

                    <!-- 操作按钮 -->
                    <div class="flex items-center justify-between pt-2 border-t border-gray-200">
                        <div class="flex items-center gap-2">
                            <span class="text-sm text-gray-600">评价文档质量：</span>
                            <div class="flex gap-1">
                                <el-tooltip content="认为这个文档很有帮助" placement="top">
                                    <el-button
                                        type="success"
                                        size="small"
                                        circle
                                        @click="handleRank('good', item)"
                                        class="flex items-center justify-center hover:bg-green-600 transition-colors"
                                    >
                                        <el-icon>
                                            <Top />
                                        </el-icon>
                                    </el-button>
                                </el-tooltip>
                                <el-tooltip content="认为这个文档没什么帮助" placement="top">
                                    <el-button
                                        type="danger"
                                        size="small"
                                        circle
                                        @click="handleRank('bad', item)"
                                        class="flex items-center justify-center hover:bg-red-600 transition-colors"
                                    >
                                        <el-icon>
                                            <Bottom />
                                        </el-icon>
                                    </el-button>
                                </el-tooltip>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 空状态提示 -->
            <div v-if="!documents || documents.length === 0" class="text-center py-8 text-gray-500">
                <el-icon size="48" class="mb-2 text-gray-400">
                    <Top />
                </el-icon>
                <p>暂无引用的知识库文档</p>
            </div>
        </div>
    </el-dialog>
</template>

<style scoped>
.document-item {
    animation: fadeInUp 0.3s ease-out;
}

.document-item:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

:deep(.el-dialog) {
    border-radius: 12px;
}

:deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
}

:deep(.el-dialog__body) {
    padding: 24px;
}

.el-tag {
    font-size: 12px;
    line-height: 1.4;
}

:deep(.el-button.is-circle) {
    width: 28px;
    height: 28px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}
</style>
