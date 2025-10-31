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
    <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" title="引用的知识库"
        width="900">
        <div class="h-[50vh] overflow-y-scroll">
            <div v-for="(item) in documents" :key="item.id">
                <div class="mb-4">
                    {{ item.text }}
                </div>
                <div>
                    <div class="flex justify-start items-center gap-1 mb-1">
                        <el-tag v-if="item.metadata.relevance_score">ReRank得分：{{ item.metadata.relevance_score
                        }}</el-tag>
                        <el-tag>加权得分：{{ item.metadata.weightedScore }}</el-tag>
                        <el-tag>向量得分：{{ item.metadata.normalizedSpringAiScore }}</el-tag>
                        <el-tag>关键字得分：{{ item.metadata.normalizedBm25Score }}</el-tag>
                        <el-tag>手工排名：{{ item.metadata.normalizedRankScore }}</el-tag>
                    </div>
                    <div class="flex justify-start items-center gap-1">
                        <el-tag>文件名：{{ item.metadata.filename }}</el-tag>
                        <el-icon style="font-size: 18px;margin: 0 4px; cursor: pointer;"
                            @click="handleRank('good', item)">
                            <Top />
                        </el-icon>
                        <el-icon style="font-size: 18px; cursor: pointer;" @click="handleRank('bad', item)">
                            <Bottom />
                        </el-icon>
                    </div>
                </div>
                <el-divider />
            </div>
        </div>
    </el-dialog>
</template>
