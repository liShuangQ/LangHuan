<template>
    <div class="pt-4">
        <component
            ref="currentStepRef"
            :is="currentComponent"
            @next="handleNext"
            @setNextDisabled="handleSetNextDisabled"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from "vue";
import Step1_file from "./Step1_file.vue";
import Step1_text from "./Step1_text.vue";
import stepData from "./stepData";

const emit = defineEmits(["next", "setNextDisabled"]);
const currentStepRef = ref<any>(null);

// 根据stepData中的fileType来决定显示哪个组件
const currentComponent = computed(() => {
    const fileType: FileType = stepData.value?.fileType;
    console.log(stepData.value, "fileTypefileType");

    if (fileType === "file") {
        return Step1_file;
    } else if (fileType === "text") {
        return Step1_text;
    }
    // 默认显示文件上传组件
    return Step1_file;
});

const handleNext = () => {
    emit("next");
};

const handleSetNextDisabled = (disabled: boolean) => {
    emit("setNextDisabled", disabled);
};

// 暴露给父组件的方法
const init = () => {
    nextTick(() => {
        currentStepRef.value?.init && currentStepRef.value.init();
    });
};

const exportData = () => {
    currentStepRef.value?.exportData && currentStepRef.value.exportData();
};



defineExpose({ init, exportData});
</script>
