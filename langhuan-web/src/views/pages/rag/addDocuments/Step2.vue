<template>
    <div class="mt-4">
        <div class=" text-red-500 mb-2 ">注：添加后在文件管理中查看</div>
        <div class=" text-green-500 mb-2 text-sm">{{ fileChangeTit }}</div>
        <ElementFormC ref="fileFormRef" :formConfig="formConfig" :formItemConfig="formConfigData" @handle="formHandle">
        </ElementFormC>
    </div>
</template>

<script setup lang="ts">
import { http } from "@/plugins/axios";
import stepData from './stepData'
import { formConfig, formItemConfig, getFileGroupOption, fileNowOptionCache } from "./addFileFormconfig";
import {
    FormItemConfigs,
    FormDefineExpose,
} from "@/components/globalComponents/ElementFormC/form-component";
const emit = defineEmits(['next', 'end', 'setNextDisabled', 'formHandle'])
const fileFormRef = ref<FormDefineExpose>()
let fileChangeTit = ref<string>('')
let fileChangeSelectData: any = {}
let formConfigData = formItemConfig(fileFormRef)
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
    if (type === 'change') {
        if (key === 'fileName') {
            fileChangeSelectData = fileNowOptionCache.value.find((e: any) => e.fileName === data)

            if (fileChangeSelectData) {
                fileChangeTit.value = '将添加到现有文件'
                fileFormRef.value?.setFormOption([
                    {
                        key: "id",
                        value: fileChangeSelectData.id,
                    },
                    {
                        key: "fileName",
                        value: fileChangeSelectData.fileName,
                    },
                    {
                        key: "fileType",
                        value: fileChangeSelectData.fileType,
                    },
                    {
                        key: "fileSize",
                        value: fileChangeSelectData.fileSize,
                    },
                    {
                        key: "documentNum",
                        value: String(Number(fileChangeSelectData.documentNum) + stepData.value.documentData.length),
                    },
                    {
                        key: "fileDesc",
                        value: fileChangeSelectData.fileDesc,
                        disabled: true,
                    },
                    {
                        key: "fileGroupId",
                        value: Number(fileChangeSelectData.fileGroupId),
                        disabled: true,
                    }
                ])
            } else {
                fileChangeTit.value = '将创建新文件'
                fileFormRef.value?.setFormOption([
                    {
                        key: "id",
                        value: 0,
                    },
                    {
                        key: "fileType",
                        value: '文字添加',
                    },
                    {
                        key: "fileSize",
                        value: '无',
                    },
                    {
                        key: "documentNum",
                        value: stepData.value.documentData.length,
                    },
                    {
                        key: "fileDesc",
                        value: '',
                        disabled: false,
                    },
                    {
                        key: "fileGroupId",
                        value: '',
                        disabled: false,
                    }
                ])
            }

            console.log(fileChangeSelectData, 'fileChangeSelectDatafileChangeSelectData');
            console.log(fileFormRef.value!.getFromValue(), 'fileFormRef.value!.getFromValue(),');

        }

    }
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
