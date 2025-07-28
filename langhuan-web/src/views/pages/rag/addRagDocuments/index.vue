<template>
    <div class="rag-upload-container">
        <el-steps :active="activeStep" finish-status="success" simple>
            <el-step title="选择类型" />
            <el-step title="输入信息" />
            <el-step title="分词预览" />
            <el-step title="文档微调" />
            <el-step title="插入向量库" />
        </el-steps>

        <component
            class="h-[72vh] border-b-2 border-b-cyan-70"
            ref="stepRef"
            :is="currentStepComponent"
            @next="handleNext"
            @end="submitEnd"
            @prev="handlePrev"
            @setNextDisabled="setNextDisabled"
        />

        <div class="action-buttons flex justify-between align-center">
            <div class="text-left" v-html="tipText"></div>
            <div>
                <el-button v-if="activeStep > 0" @click="handlePrev"
                    >上一步</el-button
                >
                <el-button
                    :disabled="nextDisabled"
                    :type="activeStep === 4 ? 'success' : 'primary'"
                    @click="handleNext"
                >
                    {{ activeStep === 4 ? "提交" : "下一步" }}
                </el-button>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
export default {
    auto: true,
};
</script>
<script setup lang="ts">
import { ref, computed, nextTick } from "vue";
import Step0 from "./Step0.vue";
import Step1 from "./Step1.vue";
import Step2 from "./Step2.vue";
import Step3 from "./Step3.vue";
import Step4 from "./Step4.vue";
import stepData from "./stepData";
import router from "@/router";

const activeStep = ref(0);
const steps = [Step0, Step1, Step2, Step3, Step4];

const currentStepComponent = computed(() => steps[activeStep.value]);
const fileType = ref<string>("");
const tipText = ref<string>("");
const stepRef = ref<any>(null);
const nextDisabled = ref<boolean>(true);
const handleNext = () => {
    if (activeStep.value === steps.length - 1) {
        stepRef.value.submit && stepRef.value.submit();
        return;
    }
    if (activeStep.value < steps.length - 1) {
        stepRef.value.exportData();
        fileType.value = stepData.value.fileType;
        activeStep.value++;
        nextTick(() => {
            stepRef.value.init && stepRef.value.init();
        });
        nextDisabled.value = true;
    }
};
const submitEnd = () => {
    activeStep.value = 0;
    stepData.value = {
        file: null,
        fileType: "text",
    };
};

const handlePrev = () => {
    if (activeStep.value === 2) {
        ElMessageBox.confirm("后退将会丢失文件选择，是否继续?", "通知", {
            confirmButtonText: "确定",
            cancelButtonText: "返回",
            type: "warning",
        })
            .then(() => {
                activeStep.value--;
                stepData.value = {
                    file: null,
                    fileType: "text",
                };
            })
            .catch(() => {
                ElMessage({
                    type: "info",
                    message: "取消删除",
                });
            });
    } else {
        activeStep.value--;
    }

    nextTick(() => {
        stepRef.value.init && stepRef.value.init();
    });
};
const setNextDisabled = (v: boolean) => {
    nextDisabled.value = v;
};

nextTick(() => {
    const { query } = router.currentRoute.value;
    if (query) {
        tipText.value = (query?.tipText ?? "") as string;
    }
});
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
