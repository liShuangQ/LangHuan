import {
    FormConfig,
    FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";

export let formConfig: FormConfig = {
    size: "default",
    labelWidth: "90px",
    labelPosition: "left",
    showMessage: true,
};
export let formItemConfig: FormItemConfigs = [
    [
        {
            value: "",
            key: "methodName",
            type: "input",
            label: "方法名",
            col: 7,
            clearable: true,
        },
        {
            value: "",
            key: "category",
            type: "input",
            label: "分类",
            col: 7,
            clearable: true,
        },
        {
            value: "",
            key: "description",
            type: "input",
            label: "描述",
            col: 7,
            clearable: true,
        },
        {
            col: 3,
            key: "button",
            type: "custom",
        },
    ],
];
