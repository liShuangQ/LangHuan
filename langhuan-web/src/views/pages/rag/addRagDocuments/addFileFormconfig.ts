import { http } from "@/plugins/axios";
import {
    FormConfig,
    FormDefineExpose,
    FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";

export let formConfig: FormConfig = {
    size: "default",
    labelWidth: "90px",
    labelPosition: "top",
    showMessage: true,
};
export let fileNowOptionCache = ref<any[]>([]);
export const formItemConfig = (
    formRef: FormDefineExpose | any
): FormItemConfigs => {
    return [
        [
            {
                value: "0",
                key: "id",
                type: "input",
                label: "id",
                col: 12,
                show: false,
            },
            {
                value: "",
                key: "fileName",
                type: "select",
                option: [],
                label: "文件名称",
                rule: [
                    {
                        required: true,
                        message: "Please input",
                        trigger: "blur",
                    },
                ],
                remoteMethod: (query: string) => {
                    if (query === "") {
                        return;
                    }
                    clearTimeout((window as any).remoteSearchTimer);
                    (window as any).remoteSearchTimer = setTimeout(() => {
                        http.request<any>({
                            url: "/rag/file/query",
                            method: "post",
                            q_spinning: true,
                            data: {
                                fileName: query,
                                fileType: "",
                                fileGroupName: "",
                                pageNum: 1,
                                pageSize: 20,
                            },
                        })
                            .then((res) => {
                                formRef.value.setFormOption([
                                    {
                                        key: "fileName",
                                        option: res.data.records
                                            .filter((e: any) => e.canAdd)
                                            .map((e: any) => {
                                                return {
                                                    value: e.fileName,
                                                    label: e.fileName,
                                                };
                                            }),
                                    },
                                ]);
                                fileNowOptionCache.value = res.data.records;
                            })
                            .catch((err) => {
                                console.log(err);
                            });
                    }, 1000);
                },
                col: 12,
                disabled: true,
                clearable: true,
            },
            {
                value: "",
                key: "fileType",
                type: "input",
                label: "文件类型",
                rule: [
                    {
                        required: true,
                        message: "Please input",
                        trigger: "blur",
                    },
                ],
                col: 12,
                disabled: true,
                clearable: true,
            },
        ],
        [
            {
                value: "",
                key: "fileSize",
                type: "input",
                label: "文件大小",
                rule: [
                    {
                        required: true,
                        message: "Please input",
                        trigger: "blur",
                    },
                ],
                col: 12,
                disabled: true,
                clearable: true,
            },
            {
                value: "",
                key: "documentNum",
                type: "input",
                label: "切分文档数",
                rule: [
                    {
                        required: true,
                        message: "Please input",
                        trigger: "blur",
                    },
                ],
                col: 12,
                disabled: true,
                clearable: true,
            },
        ],
        [
            {
                value: "",
                key: "fileDesc",
                type: "input",
                textarea: true,
                autosize: {
                    minRows: 3,
                    maxRows: 5,
                },
                label: "文件说明",
                rule: [
                    {
                        required: true,
                        message: "Please input",
                        trigger: "blur",
                    },
                ],
                col: 24,
                disabled: false,
                clearable: true,
            },
        ],
        [
            {
                value: "",
                key: "fileGroupId",
                type: "select",
                option: [],
                label: "文件组",
                filterable: true,
                rule: [
                    {
                        required: true,
                        message: "Please select",
                        trigger: "change",
                    },
                ],
                col: 24,
                disabled: false,
                clearable: true,
            },
        ],
    ];
};
export const getFileGroupOption = () => {
    return http.request<any>({
        url: "/rag/file-group/getEnum",
        method: "post",
        q_spinning: true,
        q_contentType: "json",
        data: {
            isRead: false,
        },
    });
};
