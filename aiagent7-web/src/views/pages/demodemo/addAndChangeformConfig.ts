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
            col: 12,
            show: false,
            clearable: true,
        },
        {
            value: "",
            key: "methodName",
            type: "input",
            label: "方法名",
            col: 24,
            clearable: true,
        },
        {
            value: "",
            key: "content",
            type: "input",
            label: "提示词",
            textarea: true,
            col: 24,
            clearable: true,
        },
        {
            value: "",
            key: "description",
            type: "input",
            label: "描述",
            textarea: true,
            col: 24,
            clearable: true,
        },
        {
            value: "",
            key: "category",
            type: "input",
            label: "分类",
            col: 12,
            clearable: true,
        },
        {
            value: "",
            key: "createdAt",
            type: "input",
            label: "创建时间",
            disabled: true,
            col: 24,
        },
        {
            value: "",
            key: "updatedAt",
            type: "input",
            label: "更新时间",
            disabled: true,
            col: 24,
        },
    ],
];
