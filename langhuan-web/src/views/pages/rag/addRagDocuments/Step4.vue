<template>
    <div class="p-6">
        <div class="bg-amber-50 border border-amber-200 rounded-lg p-4 mb-6">
            <div class="flex items-start">
                <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-amber-600 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
                    </svg>
                </div>
                <div class="ml-3">
                    <p class="text-sm text-amber-800">
                        注：添加后在文件管理中查看
                    </p>
                </div>
            </div>
        </div>

        <div class="mb-2">
            <h2 class="text-xl font-semibold text-gray-800">{{ fileChangeTit }}</h2>
        </div>

        <div class="bg-white rounded-lg border border-gray-200 p-6">
            <ElementFormC ref="fileFormRef" :formConfig="formConfig" :formItemConfig="formConfigData" @handle="formHandle">
            </ElementFormC>
        </div>
    </div>
</template>

<script setup lang="ts">
import { http } from "@/plugins/axios";
import stepData from "./stepData";
import {
    formConfig,
    formItemConfig,
    getFileGroupOption,
    fileNowOptionCache,
} from "./addFileFormconfig";
import { FormDefineExpose } from "@/components/globalComponents/ElementFormC/form-component";
const emit = defineEmits(["next", "end", "setNextDisabled", "formHandle"]);
const fileFormRef = ref<FormDefineExpose>();
let formConfigData = formItemConfig(fileFormRef);
let fileChangeTit = ref<string>("");
let fileChangeSelectData: any = {};
// HACK
const init = async () => {
    emit("setNextDisabled", false);
    const res = await getFileGroupOption();

    if (stepData.value.fileType === "file") {
        const { file } = stepData.value;
        fileFormRef.value!.setFormOption([
            {
                key: "fileName",
                value: file.name,
                option: [
                    {
                        label: file.name,
                        value: file.name,
                    },
                ],
                optionGroup: [],
                disabled: true,
                filterable: false,
                remote: false,
                allowCreate: false,
            },
            {
                key: "fileType",
                value: file.raw.type,
            },
            {
                key: "fileSize",
                value: file.raw.size,
            },
            {
                key: "documentNum",
                value: (stepData.value.fineTuneData as any[]).length,
            },
            {
                key: "fileGroupId",
                option: res.data.map((e: any) => {
                    return {
                        label: e.groupName,
                        value: e.id,
                    };
                }),
            },
        ]);
    }
    if (stepData.value.fileType === "text") {
        fileFormRef.value!.setFormOption([
            {
                key: "fileName",
                value: "",
                option: [],
                optionGroup: [],
                disabled: false,
                filterable: true,
                remote: true,
                allowCreate: true,
            },
            {
                key: "fileType",
                value: "text",
            },
            {
                key: "fileSize",
                value: "无",
            },
            {
                key: "documentNum",
                value: (stepData.value.fineTuneData as any[]).length,
            },
            {
                key: "fileGroupId",
                option: res.data.map((e: any) => {
                    return {
                        label: e.groupName,
                        value: e.id,
                    };
                }),
            },
        ]);
    }
    if (stepData.value.fileType === "html") {
        fileFormRef.value!.setFormOption([
            {
                key: "fileName",
                value: "",
                option: [],
                optionGroup: [],
                disabled: false,
                filterable: true,
                remote: true,
                allowCreate: true,
            },
            {
                key: "fileType",
                value: "html",
            },
            {
                key: "fileSize",
                value: "无",
            },
            {
                key: "documentNum",
                value: (stepData.value.fineTuneData as any[]).length,
            },
            {
                key: "fileGroupId",
                option: res.data.map((e: any) => {
                    return {
                        label: e.groupName,
                        value: e.id,
                    };
                }),
            },
        ]);
    }
};
// HACK
const formHandle = (type: string, key: string, data: any, other: any) => {
    if (
        (stepData.value.fileType === "text" ||
            stepData.value.fileType === "html") &&
        type === "change"
    ) {
        if (key === "fileName") {
            fileChangeSelectData = fileNowOptionCache.value.find(
                (e: any) => String(e.id) === String(data)
            );
            if (fileChangeSelectData) {
                if (fileChangeSelectData.canAdd) {
                    fileChangeTit.value = "将添加到现有文件";
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
                            value: String(
                                Number(fileChangeSelectData.documentNum) +
                                (stepData.value.fineTuneData as any[])
                                    .length
                            ),
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
                        },
                    ]);
                } else {
                    fileChangeTit.value =
                        "当前文件名被占用且不可编辑，请重新编辑";
                    fileFormRef.value?.setFormOption([
                        {
                            key: "fileName",
                            value: "",
                        },
                    ]);
                }
            } else {
                fileChangeTit.value = "将创建新文件";
                fileFormRef.value?.setFormOption([
                    {
                        key: "id",
                        value: 0, //与后端约定，0代表是新的
                    },
                    // HACK
                    {
                        key: "fileType",
                        value:
                            stepData.value.fileType === "text"
                                ? "text"
                                : stepData.value.fileType === "html"
                                    ? "html"
                                    : "",
                    },
                    {
                        key: "fileSize",
                        value: "无",
                    },
                    {
                        key: "documentNum",
                        value: (stepData.value.fineTuneData as any[]).length,
                    },
                    {
                        key: "fileDesc",
                        value: "",
                        disabled: false,
                    },
                    {
                        key: "fileGroupId",
                        value: "",
                        disabled: false,
                    },
                ]);
            }
        }
    } else {
        fileFormRef.value!.setFormOption([
            {
                key: "id",
                value: 0, //与后端约定，0代表是新的
            },
        ]);
    }
};
const submit = () => {
    if (!fileFormRef.value!.submitForm()) {
        return;
    }
    http.request<any>({
        url: "/rag/writeDocumentsToVectorStore",
        method: "post",
        q_spinning: true,
        q_contentType: "json",
        data: {
            documents: stepData.value.fineTuneData,
            ragFile: {
                ...fileFormRef.value!.getFromValue(),
            },
        },
    })
        .then((res) => {
            if (res.data.indexOf("成功") !== -1) {
                ElMessage.success("插入成功");
                emit("end");
            } else {
                ElMessage.error("插入失败");
            }
        })
        .catch((err) => {
            console.log(err);
        });
};
defineExpose({ init, submit });
</script>
