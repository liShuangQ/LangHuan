<template>
    <div class="rag-upload-container">
        <el-steps :active="activeStep" finish-status="success" simple>
            <el-step title="选择文件&分词方式" />
            <el-step title="预览修改" />
            <el-step title="插入向量库" />
        </el-steps>

        <component ref="stepRef" :is="currentStepComponent" @next="handleNext" @prev="handlePrev" />

        <div class="action-buttons">
            <!-- <el-button v-if="activeStep > 0" @click="handlePrev">上一步</el-button> -->
            <el-button v-if="activeStep < 3" type="primary" @click="handleNext">
                下一步
            </el-button>
            <el-button v-if="activeStep === 3" type="success" @click="handleSubmit">
                提交
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
import Step3 from './Step3.vue'
import { http } from "@/plugins/axios";
const activeStep = ref(0)
const steps = [Step1, Step2, Step3]

const currentStepComponent = computed(() => steps[activeStep.value])
const stepRef = ref<any>(null)

const handleNext = () => {
    if (activeStep.value === 0) {
        http.request<any>({
            url: '/rag/readAndSplitDocument',
            method: 'post',
            q_spinning: true,
            q_contentType: 'formfile',
            data: stepRef.value.exportData(),
        }).then(res => {
            if (activeStep.value < steps.length - 1) {
                activeStep.value++
            }
            nextTick(() => {
                stepRef.value.setDocument(res.data)
            })
        }).catch(err => {
            console.log(err)
        })
    }
}

const handlePrev = () => {
    if (activeStep.value > 0) {
        activeStep.value--
    }
}

const handleSubmit = () => {
    // 处理提交逻辑
    console.log('文件提交')
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