<template>
    <div class="pt-4">
        <div class="flex justify-center items-center h-full">
            <div class="w-full max-w-md">
                <h3 class="text-xl font-semibold mb-6 text-center">
                    选择输入方式
                </h3>
                <!-- HACK -->
                <div class="space-y-6">
                    <div
                        @click="selectType('file')"
                        :class="[
                            'w-full p-4 border-2 rounded-xl cursor-pointer transition-all duration-200',
                            selectedType === 'file'
                                ? 'border-blue-500 bg-blue-50 shadow-md'
                                : 'border-gray-300 hover:border-blue-400 hover:shadow-sm',
                        ]"
                    >
                        <div class="flex items-center">
                            <div class="flex items-center mr-4">
                                <div
                                    :class="[
                                        'w-5 h-5 rounded-full border-2 flex items-center justify-center',
                                        selectedType === 'file'
                                            ? 'border-blue-500 bg-blue-500'
                                            : 'border-gray-400',
                                    ]"
                                >
                                    <div
                                        v-if="selectedType === 'file'"
                                        class="w-2 h-2 bg-white rounded-full"
                                    ></div>
                                </div>
                            </div>
                            <div>
                                <div class="font-semibold text-lg mb-1">
                                    文件上传
                                </div>
                                <div class="text-sm text-gray-500">
                                    支持上传txt、pdf、docx、md等格式文件
                                </div>
                            </div>
                        </div>
                    </div>
                    <div
                        @click="selectType('text')"
                        :class="[
                            'w-full p-4 border-2 rounded-xl cursor-pointer transition-all duration-200',
                            selectedType === 'text'
                                ? 'border-green-500 bg-green-50 shadow-md'
                                : 'border-gray-300 hover:border-green-400 hover:shadow-sm',
                        ]"
                    >
                        <div class="flex items-center">
                            <div class="flex items-center mr-4">
                                <div
                                    :class="[
                                        'w-5 h-5 rounded-full border-2 flex items-center justify-center',
                                        selectedType === 'text'
                                            ? 'border-green-500 bg-green-500'
                                            : 'border-gray-400',
                                    ]"
                                >
                                    <div
                                        v-if="selectedType === 'text'"
                                        class="w-2 h-2 bg-white rounded-full"
                                    ></div>
                                </div>
                            </div>
                            <div>
                                <div class="font-semibold text-lg mb-1">
                                    文本输入
                                </div>
                                <div class="text-sm text-gray-500">
                                    直接输入文本内容
                                </div>
                            </div>
                        </div>
                    </div>
                    <div
                        @click="selectType('html')"
                        :class="[
                            'w-full p-4 border-2 rounded-xl cursor-pointer transition-all duration-200',
                            selectedType === 'html'
                                ? 'border-green-500 bg-green-50 shadow-md'
                                : 'border-gray-300 hover:border-green-400 hover:shadow-sm',
                        ]"
                    >
                        <div class="flex items-center">
                            <div class="flex items-center mr-4">
                                <div
                                    :class="[
                                        'w-5 h-5 rounded-full border-2 flex items-center justify-center',
                                        selectedType === 'html'
                                            ? 'border-green-500 bg-green-500'
                                            : 'border-gray-400',
                                    ]"
                                >
                                    <div
                                        v-if="selectedType === 'html'"
                                        class="w-2 h-2 bg-white rounded-full"
                                    ></div>
                                </div>
                            </div>
                            <div>
                                <div class="font-semibold text-lg mb-1">
                                    链接输入
                                </div>
                                <div class="text-sm text-gray-500">
                                    直接输入链接URL
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, toRaw } from "vue";
import { Upload, Edit } from "@element-plus/icons-vue";
import stepData from "./stepData";

const emit = defineEmits(["next", "setNextDisabled"]);
let selectedType = ref<FileType>("file");

const selectType = (type: FileType) => {
    selectedType.value = type;
};

watch(
    selectedType,
    (newValue, oldValue) => {
        if (newValue) {
            emit("setNextDisabled", false);
        } else {
            emit("setNextDisabled", true);
        }
    },
    { immediate: true }
);

const init = () => {
    if (stepData.value?.fileType) {
        selectedType.value = stepData.value.fileType;
        emit("setNextDisabled", false);
    } else {
        emit("setNextDisabled", true);
    }
};

const exportData = () => {
    stepData.value = {
        file: null,
        fileType: selectedType.value,
    };
};

defineExpose({ init, exportData });
</script>
