import { http } from "@/plugins/axios";
import {
    FormConfig,
    FormItemConfigs,
    FormDefineExpose,
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
                value: "",
                key: "id",
                type: "input",
                label: "id",
                col: 12,
                show:false,
            },
            {
                value: "",
                key: "fileName",
                type: "select",
                label: "文档名称",
                option: [],
                filterable: true,
                remote: true,
                allowCreate: true,
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
                                fileGroupId: "",
                                pageNum: 1,
                                pageSize: 10,
                            },
                        })
                            .then((res) => {
                                formRef.value.setFormOption([
                                    {
                                        key: "fileName",
                                        option: res.data.records.map(
                                            (e: any) => {
                                                return {
                                                    value: e.fileName,
                                                    label: e.fileName,
                                                };
                                            }
                                        ),
                                    },
                                ]);
                                fileNowOptionCache.value = res.data.records;
                            })
                            .catch((err) => {
                                console.log(err);
                            });
                    }, 1000);
                },
                rule: [
                    {
                        required: true,
                        message: "Please input",
                        trigger: "blur",
                    },
                ],
                col: 12,
                clearable: true,
            },
            {
                value: "文字添加",
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
                value: "无",
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
        data: {},
    });
};
