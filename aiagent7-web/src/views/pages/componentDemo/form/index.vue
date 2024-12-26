<template>
    <div>
        <ElementFormC
            ref="formComRef"
            :formConfig="formConfig"
            :formItemConfig="formItemConfig"
            @handle="formHandle"
        >
            <template #prepend-inputkey>11</template>
            <template #append-inputkey>22</template>
            <template #optionCustom-selectkey="{item}">
                <span style="float: left">{{ item.label }}</span>
                <span style="float: right;color: var(--el-text-color-secondary);font-size: 13px;">
                                    {{ item.value }}
                </span>
            </template>


            <template #custom-customkey="props">
                <span class="text-red-300 float-left leading-8"
                >自定义插槽{{ props }}</span
                >
            </template>
            <template #suffix-autocompletekey>
                <el-icon class="el-input__icon">
                    <edit/>
                </el-icon>
            </template>
            <template #action-switchkey>T</template>
            <template #inactive-switchkey>F</template>
        </ElementFormC>
        <el-button @click="getFromValue()">获取表单值</el-button>
        <el-button @click="setFormOption()">改变</el-button>
        <el-button @click="submitForm()">校验提交</el-button>
        <el-button @click="formComRef!.resetForm()">重置</el-button>
    </div>
</template>

<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
import {formConfig, formItemConfig} from "./config";
import {
    FormConfig,
    FormDefineExpose,
    FormItemConfig,
} from "@/components/globalComponents/ElementFormC/form-component";

const formComRef = ref<FormDefineExpose>();
const setFormOption = () => {
    formComRef.value!.setFormOption([
        {
            key: "selectkey",
            value: "",
            option: [
                {
                    label: "111",
                    value: "111",
                },
                {
                    label: "222",
                    value: "222",
                },
            ],
            rule: [],
        },
    ]);
};

const submitForm = () => {
    formComRef
        .value!.submitForm()
        .then((res) => {
            console.log(res, "成功");
        })
        .catch((rej: any) => {
            console.log(rej, "失败");
            Object.keys(rej).forEach((k) => {
                rej[k].forEach((e: any) => {
                    ElMessage.warning(e.message);
                });
            });
        });
};
const getFromValue = () => {
    console.log(formComRef.value!.getFromValue());
};
const formHandle = (type: string, key: string, data: any, other: any) => {
    console.log(type, key, data, other);
    if (type === 'autocomplete' && key === 'autocompletekey') {
        other([{value: '1'}, {value: '2'}])
    }
};
</script>

<style scoped></style>
