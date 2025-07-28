<template>
    <div class="border-box pt-2 w-full h-full">
        <div class="h-full flex justify-center items-center flex-col">
            <div class="text-2xl font-semibold mb-2">请输入您的链接</div>
            <div>
                <el-input
                    style="width: 600px"
                    size="large"
                    v-model="htmlData"
                    placeholder="请输入您的链接"
                />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import stepData from "./stepData";

const emit = defineEmits(["next", "setNextDisabled"]);
let htmlData = ref("");
const init = () => {
    if (stepData.value.text) {
        htmlData.value = stepData.value.text;
        emit("setNextDisabled", true);
    } else {
        emit("setNextDisabled", false);
    }
};

watch(
    () => htmlData.value,
    (newValue, oldValue) => {
        if (newValue) {
            emit("setNextDisabled", false);
        } else {
            emit("setNextDisabled", true);
        }
    },
    { immediate: true, deep: true }
);

const exportData = () => {
    stepData.value = {
        ...toRaw(stepData.value),
        html: toRaw(htmlData.value).trim(),
    };
};

defineExpose({ init, exportData });
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>
