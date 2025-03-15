import { http } from "@/plugins/axios";
import {
    FormConfig,
    FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";

export let formConfig: FormConfig = {
    size: "default",
    labelWidth: "90px",
    labelPosition: "top",
    showMessage: true,
};
export let formItemConfig: FormItemConfigs = [
    [
        {
            value: "",
            key: "id",
            type: "input",
            label: "id",
            col: 12,
            show: false,
            clearable: true,
        },
        {
            value: "",
            key: "fileName",
            type: "input",
            label: "文件名称",
            rule: [
                { required: true, message: "Please input", trigger: "blur" },
            ],
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
                { required: true, message: "Please input", trigger: "blur" },
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
                { required: true, message: "Please input", trigger: "blur" },
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
                { required: true, message: "Please input", trigger: "blur" },
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
                { required: true, message: "Please input", trigger: "blur" },
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
                { required: true, message: "Please select", trigger: "change" },
            ],
            col: 24,
            disabled: false,
            clearable: true,
        },
    ],
];

export const getFileGroupOption = () => {
    return http.request<any>({
        url: '/rag/file-group/getEnum',
        method: 'post',
        q_spinning: true,
        q_contentType: 'json',
        data: {},
    })
}