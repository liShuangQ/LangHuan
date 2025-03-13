<template>
    <div class="step-container">
        <el-upload drag :on-change="handleChange" :auto-upload="false" :limit="1" :on-exceed="handleExceed"
            :on-remove="handleRemove">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
                <span v-if="!selectedFile">将文件拖到此处，或<em>点击上传</em></span>
                <span v-else>已选择</span>
            </div>
            <template #tip>
                <div class="el-upload__tip">
                    支持上传txt、pdf、docx等格式文件
                </div>
            </template>
        </el-upload>
        <el-radio-group v-model="splitFileMethod">
            <el-radio border label="FixedWindowTextSplitter">固定窗口切分</el-radio>
            <el-radio border label="PatternTokenTextSplitter">正则切分</el-radio>
        </el-radio-group>
        <button @click="exportData">123123</button>
    </div>
</template>

<style scoped>
.step-container {
    margin-top: 20px;
}

.small-upload {
    width: 300px;
    height: 150px;
}
</style>

<script setup lang="ts">
import { UploadFilled } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['next'])
let selectedFile = ref<any>(null)

const handleChange = (file: any) => {
    selectedFile.value = file
}

const handleExceed = () => {
    ElMessage.warning('只能上传一个文件')
}
const handleRemove = () => {
    selectedFile.value = null
}

const splitFileMethod = ref('FixedWindowTextSplitter')

const exportData = () => {
    console.log(selectedFile.value,'selectedFile.valueselectedFile.value');

    let formData = new FormData()
    formData.append('file', selectedFile.value.raw)
    formData.append('splitFileMethod', splitFileMethod.value)
    formData.append('methodData', JSON.stringify({ windowSize: 20 }))
    return formData
}
defineExpose({ exportData })
</script>