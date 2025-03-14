<template>
    <div class=" border-box pt-2 w-full">
        <div class=" h-full w-full overflow-y-scroll ">
            <el-input v-for="(item, index) in fineTuneData" :key="index" v-model="fineTuneData[index]" autosize
                type="textarea" />
        </div>

    </div>
</template>

<script setup lang="ts">
import stepData from './stepData'

const emit = defineEmits(['next', 'setNextDisabled'])
let fineTuneData = ref([])
const init = () => {
    stepData.value.previewADocument && (fineTuneData.value = stepData.value.previewADocument)

    emit('setNextDisabled', false)
}

const exportData = () => {
    stepData.value = {
        ...toRaw(stepData.value),
        fineTuneData: toRaw(fineTuneData.value)
    }
}
defineExpose({ init, exportData })
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>