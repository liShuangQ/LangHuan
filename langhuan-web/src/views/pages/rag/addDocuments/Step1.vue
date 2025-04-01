<template>
    <div class=" border-box pt-2 w-full">
        <div class=" h-full w-full overflow-y-scroll ">
            <el-input class=" mb-2" v-for="(item, index) in documentData" :key="index" v-model="documentData[index]"
                :autosize="{ minRows: 7, maxRows: 10 }" placeholder="请输入你的文档" type="textarea" maxlength="2000"
                show-word-limit />
        </div>

    </div>
</template>

<script setup lang="ts">
import stepData from './stepData'

const emit = defineEmits(['next', 'setNextDisabled'])
let documentData = ref([''])
const init = () => {

    if (stepData.value.documentData[0]) {
        documentData.value = stepData.value.documentData
        emit('setNextDisabled', true)
    } else {
        emit('setNextDisabled', false)
    }
}

watch(() => documentData.value, (newValue, oldValue) => {
    if (newValue.length > 0 && newValue.every(e => e)) {
        emit('setNextDisabled', false)
    } else {
        emit('setNextDisabled', true)
    }
}, { immediate: true, deep: true })

const exportData = () => {
    stepData.value = {
        ...toRaw(stepData.value),
        documentData: toRaw(documentData.value)
    }
}
const addDocument = () => {
    documentData.value.push('')
}
defineExpose({ init, exportData, addDocument })
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>