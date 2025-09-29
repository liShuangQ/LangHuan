<template>
    <div class="pt-4">
        <el-upload
            drag
            :on-change="handleChange"
            :auto-upload="false"
            :limit="1"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload"
            :on-remove="handleRemove"
            accept=".txt,.md,.markdown,.docx"
        >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
                <span v-if="!selectedFile"
                    >将文件拖到此处，或<em>点击上传</em></span
                >
                <span v-else>已选择</span>
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    推荐上传docx格式文件。支持上传txt、markdown、docx格式文件，文件大小不超过10MB。
                </div>
            </template>
        </el-upload>
    </div>
</template>

<script setup lang="ts">
import { UploadFilled } from "@element-plus/icons-vue";
import { ref, watch, toRaw } from "vue";
import { ElMessage } from "element-plus";
import stepData from "./stepData";

const emit = defineEmits(["next", "setNextDisabled"]);
let selectedFile = ref<any>(null);

const handleChange = (file: any) => {
    selectedFile.value = file;
};
const handleExceed = () => {
    ElMessage.warning("只能上传一个文件");
};
const handleRemove = () => {
    selectedFile.value = null;
};

const beforeUpload = (file: File) => {
    const maxSize = 10 * 1024 * 1024; // 10MB
    const allowedTypes = [
        'text/plain', // .txt
        'text/markdown', // .markdown
        'text/x-markdown', // .md
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document' // .docx
    ];

    // 检查文件大小
    if (file.size > maxSize) {
        ElMessage.error("文件大小不能超过10MB");
        return false;
    }

    // 检查文件类型
    if (!allowedTypes.includes(file.type)) {
        ElMessage.error("只支持txt、markdown、docx格式的文件");
        return false;
    }

    return true;
};

watch(selectedFile, (newValue, oldValue) => {
    if (newValue) {
        emit("setNextDisabled", false);
    } else {
        emit("setNextDisabled", true);
    }
});

const exportData = () => {
    stepData.value = {
        ...stepData.value,
        file: toRaw(selectedFile.value),
    };
};
defineExpose({ exportData });
</script>
