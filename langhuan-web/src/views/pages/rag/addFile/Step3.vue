<template>
    <div class="border-box pt-2 w-full flex flex-col h-[72vh]">
        <div class="overflow-y-auto h-[70vh]">
            <el-input class="mb-2" v-for="(item, index) in paginatedData" :key="index"
                v-model="fineTuneData[(currentPage - 1) * pageSize + index]" autosize type="textarea" />
        </div>

        <div class="border-t-2 border-b-cyan-70">
            <el-pagination class="my-4 pr-2 float-right " size="small" v-model:current-page="currentPage"
                v-model:page-size="pageSize" :page-sizes="[10]" :total="fineTuneData.length"
                layout="total, sizes, prev, pager, next" background />
        </div>
    </div>
</template>

<script setup lang="ts">
import stepData from './stepData'
import { computed, ref } from 'vue'

const emit = defineEmits(['next', 'setNextDisabled'])
const fineTuneData = ref<string[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedData = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return fineTuneData.value.slice(start, end)
})

const init = () => {
    stepData.value.previewADocument && (fineTuneData.value = stepData.value.previewADocument)
    currentPage.value = 1
    emit('setNextDisabled', false)
}

const exportData = () => {
    stepData.value = {
        ...toRaw(stepData.value),
        fineTuneData: toRaw(fineTuneData.value).filter(item => item && item.trim() !== '')
    }
}
defineExpose({ init, exportData })
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>