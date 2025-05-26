import {
    FormConfig,
    FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";

export let formConfig: FormConfig = {
    size: "default",
    labelWidth: "70px",
    labelPosition: "left",
    showMessage: true,
};
export let formItemConfig: FormItemConfigs = [
    [
        {
            value: "",
            key: "fileName",
            type: "input",
            label: "文件名称",
            col: 5,
            clearable: true,
        },
        {
            value: "",
            key: "fileType",
            type: "input",
            label: "文件类型",
            col: 5,
            clearable: true,
        },
        {
            value: "",
            key: "fileGroupName",
            type: "input",
            label: "文件组",
            col: 5,
            clearable: true,
        },
        {
            col: 9,
            key: "button",
            type: "custom",
        },
    ],
];
