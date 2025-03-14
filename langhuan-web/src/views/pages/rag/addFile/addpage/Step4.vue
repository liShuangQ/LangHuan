<template>
    <div class="step-container">
        <el-form :model="form" label-width="200px">
            <el-form-item label="文件组">
                <el-select v-model="form.group" placeholder="请选择文件组" @change="groupChange">
                    <el-option label="组A" value="groupA" />
                    <el-option label="组B" value="groupB" />
                </el-select>
            </el-form-item>

            <el-form-item label="附加信息(MetaData)">
                <div class="flex gap-2 w-full pb-2" v-for="(item, index) in form.additionalInfo" :key="index">
                    <el-input :disabled="disabledKeys.includes(item.key)" style="width: 200px;" v-model="item.key"
                        placeholder="请输入key" />
                    <el-input :disabled="disabledKeys.includes(item.key)" v-model="item.value" placeholder="请输入value" />
                    <el-button v-if="!disabledKeys.includes(item.key)" type="danger" :icon="Delete" circle
                        @click="removeAdditionalInfo(index)" />
                </div>

            </el-form-item>
            <div>
                <el-button class="float-right"
                    @click="form.additionalInfo.push({ key: '', value: '' })">新增附加信息</el-button>
            </div>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { http } from "@/plugins/axios";
import stepData from './stepData'
import {
    Delete,
} from '@element-plus/icons-vue'
const emit = defineEmits(['next', 'end', 'setNextDisabled'])
const form = ref<any>({
    group: '',
    additionalInfo: []
})

const removeAdditionalInfo = (index: number) => {
    form.value.additionalInfo.splice(index, 1)
}
const disabledKeys = ['fileName', 'fileType', 'fileSize', 'fileId', 'parentFileId']
const init = () => {
    const { file } = stepData.value
    form.value.additionalInfo = [
        { key: 'fileName', value: file.name },
        { key: 'fileType', value: file.raw.type },
        { key: 'fileSize', value: file.raw.size },
        { key: 'fileId', value: file.raw.uid }
    ]
}

const groupChange = (value: string) => {
    let sw: boolean = false
    form.value.additionalInfo.forEach((e: any) => {
        if (e.key === 'parentFileId') {
            sw = true
            e.value = value
        }
    });
    !sw && form.value.additionalInfo.unshift({ key: 'parentFileId', value })
    emit('setNextDisabled', false)
}
const submit = () => {
    let metadata: any = {}
    form.value.additionalInfo.forEach((e: { key: string, value: string }) => {
        metadata[e.key as string] = e.value
    });
    http.request<any>({
        url: '/rag/writeDocumentsToVectorStore',
        method: 'post',
        q_spinning: true,
        q_contentType: 'json',
        data: {
            documents: stepData.value.fineTuneData,
            metadata: metadata
        },
    }).then(res => {
        if (res.data.indexOf('成功') !== -1) {
            ElMessage.success('插入成功')
            emit('end')
        } else {
            ElMessage.error('插入失败')
        }
    }).catch(err => {
        console.log(err)
    })
}
defineExpose({ init, submit })
</script>

<style scoped>
.step-container {
    margin-top: 20px;
}
</style>