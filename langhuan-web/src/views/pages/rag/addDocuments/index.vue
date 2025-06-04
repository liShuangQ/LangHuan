<template>
    <div class="rag-upload-container">
        <div v-if="feedbackTipText" class=" text-red-500 mb-2">{{ feedbackTipText }}</div>
        <el-steps :active="activeStep" finish-status="success" simple>
            <el-step title="输入文字" />
            <el-step title="插入向量库" />
        </el-steps>

        <component class="h-[70vh] border-b-2 border-b-cyan-70" ref="stepRef" :is="currentStepComponent"
            @next="handleNext" @end="submitEnd" @prev="handlePrev" @setNextDisabled="setNextDisabled" />

        <div class="action-buttons">
            <el-button v-if="activeStep > 0" @click="handlePrev">上一步</el-button>
            <el-button @click="cacheDocument">缓存文本</el-button>
            <el-button @click="getCacheDocument">获取缓存</el-button>
            <el-button v-if="activeStep === 0" @click="zeroAddDocument">添加</el-button>
            <el-button :disabled="nextDisabled" :type="activeStep === 3 ? 'success' : 'primary'" @click="handleNext">
                {{ activeStep === 1 ? '提交' : '下一步' }}
            </el-button>
        </div>
    </div>
</template>
<script lang="ts">
export default {
    auto: true,
};
</script>
<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import Step1 from './Step1.vue'
import Step2 from './Step2.vue'
import stepData from './stepData'
import { useRouter } from 'vue-router';
const router = useRouter();
let feedbackTipText = ref<string>('')
const { query } = router.currentRoute.value
if (query.tipText) {
    feedbackTipText.value = query.tipText as string;
}

const activeStep = ref(0)
const steps = [Step1, Step2]

const currentStepComponent = computed(() => steps[activeStep.value])
const stepRef = ref<any>(null)
const nextDisabled = ref<boolean>(true)



const handleNext = () => {
    // 提交
    if (activeStep.value === steps.length - 1) {
        stepRef.value.submit && stepRef.value.submit()
        return;
    }
    if (activeStep.value < steps.length - 1) {
        stepRef.value.exportData()
        activeStep.value++
        nextTick(() => {
            stepRef.value.init && stepRef.value.init()
        })
        nextDisabled.value = true
    }
}
const submitEnd = () => {
    activeStep.value = 0
    stepData.value = null
    localStorage.removeItem('ragTextDocument')
}

const handlePrev = () => {
    activeStep.value--
    nextTick(() => {
        stepRef.value.init && stepRef.value.init()
    })
}
const setNextDisabled = (v: boolean) => {
    nextDisabled.value = v
}

const zeroAddDocument = () => {
    stepRef.value.addDocument && stepRef.value.addDocument()
}
const cacheDocument = () => {
    // HACK documentData
    stepRef.value.exportData()
    localStorage.setItem('ragTextDocument', JSON.stringify(toRaw(stepData.value.documentData)))
    ElMessage.success('缓存成功')
}
const getCacheDocument = () => {
    if (localStorage.getItem('ragTextDocument')) {
        stepRef.value.cacheDocument && stepRef.value.cacheDocument(JSON.parse(localStorage.getItem('ragTextDocument') as any))
        ElMessage.success('获取缓存成功')
    } else {
        ElMessage.error('没有缓存')
    }
}


</script>

<style scoped>
.rag-upload-container {
    margin: 0 auto;
    padding: 20px;
}

.action-buttons {
    margin-top: 20px;
    text-align: right;
}
</style>