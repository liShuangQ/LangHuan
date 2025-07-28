<template>
    <div class="border-box pt-2 w-full">
        <div class="h-full w-full overflow-y-scroll">
            <el-input
                class="mb-2"
                v-model="documentData"
                :autosize="{ minRows: 24, maxRows: 24 }"
                placeholder="请输入你的文档"
                type="textarea"
                show-word-limit
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import stepData from "./stepData";

const emit = defineEmits(["next", "setNextDisabled"]);
let documentData = ref("");
const init = () => {
    if (stepData.value.text) {
        documentData.value = stepData.value.text;
        emit("setNextDisabled", true);
    } else {
        emit("setNextDisabled", false);
    }
};

watch(
    () => documentData.value,
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
        text: toRaw(documentData.value).trim(),
    };
};

defineExpose({ init, exportData });
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>
