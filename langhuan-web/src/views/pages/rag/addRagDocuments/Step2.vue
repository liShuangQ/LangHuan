<template>
    <div class="flex border-box py-2">
        <div class="w-2/5 border-r-2 border-b-cyan-950 border-box pr-2">
            <!-- ---------- -->
            <div class="mb-4">
                <div class="py-2 text-xl">拆分方式</div>
                <el-radio-group v-model="splitFileMethod">
                    <el-radio border label="FixedWindowTextSplitter"
                        >固定窗口切分</el-radio
                    >
                    <el-radio border label="PatternTokenTextSplitter"
                        >正则切分</el-radio
                    >
                    <!-- <el-radio border label="OpenNLPSentenceSplitter">NLP切分</el-radio> -->
                    <!-- <el-radio border label="LlmTextSplitter">大模型切分</el-radio> -->
                </el-radio-group>
            </div>
            <!-- ---------- -->
            <div class="mb-4">
                <div class="py-2 text-xl">参数选择</div>
                <div v-if="splitFileMethod === 'FixedWindowTextSplitter'">
                    <div class="text-xs">窗口大小</div>
                    <el-slider
                        v-model="methodData.FixedWindowTextSplitter.windowSize"
                        show-input
                        size="small"
                        :min="50"
                        :max="1000"
                        :step="50"
                        show-stops
                    />
                </div>
                <div v-else-if="splitFileMethod === 'PatternTokenTextSplitter'">
                    <div class="text-xs">正则表达式</div>
                    <el-input
                        v-model="
                            methodData.PatternTokenTextSplitter.splitPattern
                        "
                        size="small"
                    />
                </div>
                <div
                    v-else-if="splitFileMethod === 'OpenNLPSentenceSplitter'"
                ></div>
                <div v-else-if="splitFileMethod === 'LlmTextSplitter'">
                    <div class="text-xs">窗口大小</div>
                    <el-slider
                        v-model="methodData.LlmTextSplitter.windowSize"
                        show-input
                        size="small"
                        :min="50"
                        :max="1000"
                        :step="50"
                        show-stops
                    />
                    <div class="text-xs">选择模型</div>
                    <el-select
                        v-model="methodData.LlmTextSplitter.modelName"
                        placeholder="选择模型"
                    >
                        <el-option
                            v-for="item in chatModelOption"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </div>
                <div v-else>
                    <span class="text-2xl text-zinc-400">请选择拆分方式</span>
                </div>
            </div>
            <!-- ---------- -->
            <div>
                <el-button
                    class="float-right"
                    type="primary"
                    :disabled="!splitFileMethod"
                    @click="getDocument"
                    >预览</el-button
                >
            </div>
        </div>
        <div class="w-3/5 overflow-y-scroll">
            <div
                class="border-b-2 border-b-slate-300 pb-2"
                v-for="(item, index) in previewADocument"
                :key="index"
            >
                {{ item }}
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
    OpenNLPSentenceSplitter: {},
    LlmTextSplitter: {
        windowSize: 200,
        modelName: "",
    },
});

// 获取支持的模型列表
chatModelOption.value = toRaw(aimodel().getModelOptions()) as any;

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
    console.log(stepData.value, "stepData.valuestepData.valuestepData.value");

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
