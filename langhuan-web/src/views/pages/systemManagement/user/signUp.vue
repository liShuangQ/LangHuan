<script setup lang="ts">
import {
    addAndChangeFormConfig,
    addAndChangeFormItemConfig
} from "./addAndChangeformConfig";
import {
    FormDefineExpose,
} from "@/components/globalComponents/ElementFormC/form-component";
import { http } from "@/plugins/axios";
const changeFormHandle = (type: string, key: string, data: any, other: any) => {
    return
    console.log(type, key, data, other);
};
const addAndChangeFormComRef = ref<FormDefineExpose>();
const emit = defineEmits(['handle'])
let addOrChange = ref('add')
const handleFun = async (t: string, d: any = null) => {
    if (t === 'add') {
        addOrChange.value = 'save_add'
        addAndChangeFormComRef.value!.resetForm()
        addAndChangeFormComRef.value!.setFormOption([
            {
                key: 'username',
                disabled: false
            }
        ])
    }
    if (t === 'change') {
        addOrChange.value = 'save_change'
        nextTick(() => {
            addAndChangeFormComRef.value!.resetForm()
            addAndChangeFormComRef.value!.setFormOption(Object.entries(d).map(([k, v]) => {
                return {
                    key: k,
                    value: String(v)
                }
            }))
            addAndChangeFormComRef.value!.setFormOption([
                {
                    key: 'username',
                    disabled: true
                },
                {
                    key: 'password',
                    value: ""
                }
            ])
        })
    }
    if (t === 'save_add') {
        addAndChangeFormComRef
            .value!.submitForm()
            .then((res) => {
                let sd: any = addAndChangeFormComRef.value!.getFromValue()
                http.request<any>({
                    url: '/user/register',
                    method: 'post',
                    q_spinning: true,
                    q_contentType: 'json',
                    data: sd,
                }).then(res => {
                    if (res.code === 200) {
                        ElMessage.success('操作成功')
                        emit('handle', 'saveEnd')
                    }
                })
            })
            .catch((rej: any) => {
                console.log(rej, "失败");
            });
    }
    if (t === 'save_change') {
        addAndChangeFormComRef
            .value!.submitForm()
            .then((res) => {
                let sd: any = addAndChangeFormComRef.value!.getFromValue()
                http.request<any>({
                    url: '/user/change',
                    method: 'post',
                    q_spinning: true,
                    q_contentType: 'json',
                    data: sd,
                }).then(res => {
                    if (res.code === 200) {
                        ElMessage.success('操作成功')
                        emit('handle', 'saveEnd')
                    }
                })
            })
            .catch((rej: any) => {
                console.log(rej, "失败");
            });
    }
    if (t === 'close') {
        emit('handle', 'close')
    }
}
defineExpose({
    handleFun
})
</script>
<template>
    <ElementFormC ref="addAndChangeFormComRef" :formConfig="addAndChangeFormConfig"
        :formItemConfig="addAndChangeFormItemConfig" @handle="changeFormHandle">
    </ElementFormC>
    <div class="flex justify-end">
        <el-button @click="handleFun('close')">取消</el-button>
        <el-button type="primary" @click="handleFun(addOrChange)">
            确定
        </el-button>
    </div>
</template>