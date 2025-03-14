<template>
    <div class=" flex border-box py-2">
        <div class=" w-2/5 border-r-2  border-b-cyan-950 border-box pr-2">
            <!-- ---------- -->
            <div class=" mb-4">
                <div class=" py-2 text-xl">拆分方式</div>
                <el-radio-group v-model="splitFileMethod">
                    <el-radio border label="FixedWindowTextSplitter">固定窗口切分</el-radio>
                    <el-radio border label="PatternTokenTextSplitter">正则切分</el-radio>
                </el-radio-group>
            </div>
            <!-- ---------- -->
            <div class=" mb-4">
                <div class=" py-2 text-xl">参数选择</div>
                <div v-if="splitFileMethod === 'FixedWindowTextSplitter'">
                    <div class=" text-xs ">窗口大小</div>
                    <el-slider v-model="methodData.FixedWindowTextSplitter.windowSize" show-input size="small" :min="50"
                        :max="1000" :step="50" show-stops />
                </div>
                <div v-else-if="splitFileMethod === 'PatternTokenTextSplitter'">
                    <div class=" text-xs ">正则表达式</div>
                    <el-input v-model="methodData.PatternTokenTextSplitter.splitPattern" size="small" />
                </div>
                <div v-else>
                    <span class=" text-2xl text-zinc-400">请选择拆分方式</span>
                </div>
            </div>
            <!-- ---------- -->
            <div>
                <el-button class=" float-right" type="primary" :disabled="!splitFileMethod"
                    @click="getDocument">预览</el-button>
            </div>
        </div>
        <div class=" w-3/5 overflow-y-scroll ">
            <div class=" border-b-2 border-b-slate-300 pb-2" v-for="(item, index) in previewADocument" :key="index"> {{
                    item }}
            </div>
        </div>
    </div>

</template>

<script setup lang="ts">
import { ref } from 'vue'
import { http } from "@/plugins/axios";
import stepData from './stepData'

const emit = defineEmits(['next', 'setNextDisabled'])
const splitFileMethod = ref()
const previewADocument = ref<string[]>([])
// HACK 根据后端定制
const methodData = ref<any>({
    FixedWindowTextSplitter: {
        windowSize: 200
    },
    PatternTokenTextSplitter: {
        splitPattern: "[;；]+\\s*"
    },
})
const getDocument = () => {
    let formData = new FormData()
    formData.append('file', stepData.value.file.raw)
    formData.append('splitFileMethod', splitFileMethod.value)
    formData.append('methodData', JSON.stringify(methodData.value[splitFileMethod.value]))
    http.request<any>({
        url: '/rag/readAndSplitDocument',
        method: 'post',
        q_spinning: true,
        q_contentType: 'formfile',
        data: formData,
    }).then(res => {
        previewADocument.value = res.data
    }).catch(err => {
        console.log(err)
    })
}
const init = () => {
    console.log(stepData.value,'stepData.valuestepData.valuestepData.value');

    stepData.value.splitFileMethod && (splitFileMethod.value = stepData.value.splitFileMethod)
    stepData.value.previewADocument && (previewADocument.value = stepData.value.previewADocument)
    stepData.value.methodData && (methodData.value = stepData.value.methodData)
}

watch(previewADocument, (newValue, oldValue) => {
    if (newValue.length > 0) {
        emit('setNextDisabled', false)
    } else {
        emit('setNextDisabled', true)
    }
})

const exportData = () => {
    stepData.value = {
        ...toRaw(stepData.value),
        splitFileMethod: toRaw(splitFileMethod.value),
        previewADocument: toRaw(previewADocument.value),
        methodData: toRaw(methodData.value),
    }
}
defineExpose({ init, exportData })
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>
