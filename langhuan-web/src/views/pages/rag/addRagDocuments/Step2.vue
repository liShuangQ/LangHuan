<template>
    <div class="flex gap-6 p-6">
        <div class="w-2/5 border-r border-gray-200 pr-6 flex flex-col">
            <!-- 拆分方式 -->
            <div class="mb-6">
                <h3 class="text-base font-medium mb-3 text-gray-700">拆分方式</h3>
                <el-radio-group v-model="splitFileMethod" class="w-full space-y-3">
                    <el-radio
                        border
                        label="FixedWindowTextSplitter"
                        style="margin-right: 0;"
                        class="w-full hover:border-blue-400 transition-colors"
                    >
                        固定窗口切分
                    </el-radio>
                    <el-radio
                        border
                        label="PatternTokenTextSplitter"
                        class="w-full hover:border-blue-400 transition-colors"
                    >
                        正则切分
                    </el-radio>
                </el-radio-group>
            </div>

            <!-- 参数选择 -->
            <div class="mb-6 flex-1">
                <h3 class="text-lg font-semibold mb-4 text-gray-800">参数选择</h3>
                <div v-if="splitFileMethod === 'FixedWindowTextSplitter'" class="space-y-4">
                    <div>
                        <label class="text-sm font-medium text-gray-700 mb-2 block">窗口大小</label>
                        <el-slider
                            v-model="methodData.FixedWindowTextSplitter.windowSize"
                            show-input
                            size="default"
                            :min="50"
                            :max="1000"
                            :step="50"
                            show-stops
                        />
                    </div>
                </div>
                <div v-else-if="splitFileMethod === 'PatternTokenTextSplitter'" class="space-y-4">
                    <div>
                        <label class="text-sm font-medium text-gray-700 mb-2 block">正则表达式</label>
                        <el-input
                            v-model="methodData.PatternTokenTextSplitter.splitPattern"
                            size="default"
                            placeholder="请输入正则表达式"
                        />
                    </div>
                </div>
                <div v-else-if="splitFileMethod === 'LlmTextSplitter'" class="space-y-4">
                    <div>
                        <label class="text-sm font-medium text-gray-700 mb-2 block">窗口大小</label>
                        <el-slider
                            v-model="methodData.LlmTextSplitter.windowSize"
                            show-input
                            size="default"
                            :min="50"
                            :max="1000"
                            :step="50"
                            show-stops
                        />
                    </div>
                    <div>
                        <label class="text-sm font-medium text-gray-700 mb-2 block">选择模型</label>
                        <el-select
                            v-model="methodData.LlmTextSplitter.modelName"
                            placeholder="选择模型"
                            class="w-full"
                        >
                            <el-option
                                v-for="item in chatModelOption"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value"
                            />
                        </el-select>
                    </div>
                </div>
                <div v-else class="flex items-center justify-center h-32">
                    <span class="text-lg text-gray-400">请选择拆分方式</span>
                </div>
            </div>

            <!-- 操作按钮 -->
            <div class="flex justify-end">
                <el-button
                    type="primary"
                    size="default"
                    :disabled="!splitFileMethod"
                    @click="getDocument"
                    class="px-6"
                >
                    预览
                </el-button>
            </div>
        </div>

        <!-- 预览区域 -->
        <div class="w-3/5 pl-6 overflow-hidden flex flex-col">
            <h3 class="text-lg font-semibold mb-4 text-gray-800">预览效果</h3>
            <div class="flex-1 overflow-y-auto border border-gray-200 rounded-lg p-4 bg-gray-50">
                <div
                    class="mb-4 p-3 bg-white border border-gray-100 rounded last:mb-0"
                    v-for="(item, index) in previewADocument"
                    :key="index"
                >
                    <div class="text-sm text-gray-700 leading-relaxed">{{ item }}</div>
                </div>
                <div v-if="previewADocument.length === 0" class="flex items-center justify-center h-32">
                    <span class="text-gray-400">暂无预览内容，请选择拆分方式并点击预览</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { http } from "@/plugins/axios";
import stepData from "./stepData";
import aimodel from "@/store/aimodel";
const emit = defineEmits(["next", "setNextDisabled"]);
const splitFileMethod = ref<SplitFileMethod>();
const previewADocument = ref<string[]>([]);
const chatModelOption = ref<{ label: string; value: string }[]>([]);
// HACK 根据后端定制
const methodData = ref<MethodData>({
    FixedWindowTextSplitter: {
        windowSize: 200,
    },
    PatternTokenTextSplitter: {
        splitPattern: "(?:={6})\\s*",
    },
    LlmTextSplitter: {
        windowSize: 200,
        modelName: "",
    },
});

// 获取支持的模型列表
chatModelOption.value = toRaw(aimodel().getModelOptions()) as any;

// HACK
const getDocumentParam = () => {
    let param: any = {};
    let url: string = "";
    let q_contentType: any = "form";
    const methodDataJson = JSON.stringify(
        methodData.value[splitFileMethod.value as SplitFileMethod]
    );
    if (stepData.value.fileType === "file") {
        url = "/rag/readAndSplitFileDocument";
        q_contentType = "formfile";
        param = new FormData();
        param.append("file", stepData.value.file.raw);
        param.append("splitFileMethod", splitFileMethod.value);
        param.append("methodData", methodDataJson);
    }
    if (stepData.value.fileType === "text") {
        url = "/rag/readAndSplitTextDocument";
        param = {
            text: stepData.value.text,
            splitFileMethod: splitFileMethod.value,
            methodData: methodDataJson,
        };
    }
    if (stepData.value.fileType === "html") {
        url = "/rag/readAndSplitHtmlDocument";
        param = {
            url: stepData.value.html,
            splitFileMethod: splitFileMethod.value,
            methodData: methodDataJson,
        };
    }
    return {
        url,
        param,
        q_contentType,
    };
};
const getDocument = () => {
    const { url, param, q_contentType } = getDocumentParam();
    http.request<any>({
        url,
        method: "post",
        q_spinning: true,
        q_contentType,
        data: param,
    })
        .then((res) => {
            previewADocument.value = res.data;
        })
        .catch((err) => {
            console.log(err);
        });
};
const init = () => {

    stepData.value.splitFileMethod &&
        (splitFileMethod.value = stepData.value.splitFileMethod);
    stepData.value.previewADocument &&
        (previewADocument.value = stepData.value.previewADocument);
    stepData.value.methodData && (methodData.value = stepData.value.methodData);
};

watch(previewADocument, (newValue, oldValue) => {
    if (newValue.length > 0) {
        emit("setNextDisabled", false);
    } else {
        emit("setNextDisabled", true);
    }
});

const exportData = () => {
    stepData.value = {
        ...toRaw(stepData.value),
        splitFileMethod: toRaw(splitFileMethod.value),
        previewADocument: toRaw(previewADocument.value),
        methodData: toRaw(methodData.value),
    };
};
defineExpose({ init, exportData });
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>
