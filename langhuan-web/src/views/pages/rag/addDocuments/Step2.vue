<template>
    <div class="mt-4">
        <div class=" text-red-200">注：添加后在文件管理中查看</div>
        <ElementFormC ref="fileFormRef" :formConfig="formConfig" :formItemConfig="formItemConfig" @handle="formHandle">
        </ElementFormC>
    </div>
</template>

<script setup lang="ts">
import { http } from "@/plugins/axios";
import stepData from './stepData'
import { formConfig, formItemConfig, getFileGroupOption } from "./addFileFormconfig";
import {
    FormDefineExpose,
} from "@/components/globalComponents/ElementFormC/form-component";
const emit = defineEmits(['next', 'end', 'setNextDisabled', 'formHandle'])
const fileFormRef = ref<FormDefineExpose>()

const init = async () => {
    emit('setNextDisabled', false)

    const res = await getFileGroupOption()
    fileFormRef.value!.setFormOption([
        {
            key: 'documentNum',
            value: stepData.value.documentData.length,
        },
        {
            key: "fileGroupId",
            option: res.data.map((e: any) => {
                return {
                    label: e.groupName,
                    value: e.id,
                }
            }),
        },
    ])
}
const formHandle = (type: string, key: string, data: any, other: any) => {
    console.log(type, key, data, other);
};
const submit = () => {
    if (!fileFormRef.value!.submitForm()) {
        return
    }
    http.request<any>({
        url: '/rag/writeDocumentsToVectorStore',
        method: 'post',
        q_spinning: true,
        q_contentType: 'json',
        data: {
            documents: stepData.value.documentData,
            ragFile: fileFormRef.value!.getFromValue(),
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
