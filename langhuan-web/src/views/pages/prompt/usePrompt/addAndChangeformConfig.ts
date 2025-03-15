import {
    FormConfig,
    FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";

export let addAndChangeFormConfig: FormConfig = {
    size: "default",
    labelWidth: "70px",
    labelPosition: "top",
    showMessage: true,
};

export let addAndChangeFormItemConfig: FormItemConfigs = [
    [
        {
            value: "",
            key: "id",
            type: "input",
            label: "id",
            rule: [
                { required: true, message: "Please input", trigger: "blur" },
            ],
            col: 12,
            show: false,
            clearable: true,
        },
        {
            value: "",
            key: "methodName",
            type: "input",
            label: "方法名",
            rule: [
                { required: true, message: "Please input", trigger: "blur" },
            ],
            col: 12,
            clearable: true,
        },
        {
            value: "",
            key: "category",
            type: "input",
            label: "分类",
            rule: [
                { required: true, message: "Please input", trigger: "blur" },
            ],
            col: 12,
            clearable: true,
        },
        {
            value: "",
            key: "content",
            type: "input",
            label: "提示词",
            rule: [
                { required: true, message: "Please input", trigger: "blur" },
            ],
            textarea: true,
            autosize:{minRows:3},
            col: 24,
            clearable: true,
        },
        {
            value: "",
            key: "description",
            type: "input",
            label: "描述",
            rule: [
                { required: true, message: "Please input", trigger: "blur" },
            ],
            textarea: true,
            col: 24,
            clearable: true,
        }
    ],
];
