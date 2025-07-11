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
            col: 5,
            clearable: true,
        },
        {
            value: "",
            key: "category",
            type: "input",
            label: "分类",
            col: 5,
            clearable: true,
        },
        {
            col: 14,
            key: "button",
            type: "custom",
        },
    ],
];
