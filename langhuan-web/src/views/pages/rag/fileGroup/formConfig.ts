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
            key: "groupName",
            type: "input",
            label: "文件组名称",
            col: 5,
            clearable: true,
        },
        {
            value: "",
            key: "groupType",
            type: "input",
            label: "文件组类型",
            col: 5,
            clearable: true,
        },
        {
            value: "",
            key: "visibility",
            type: "select",
            label: "可见性",
            option: [
                {
                    label: "私有",
                    value: "private",
                },
                {
                    label: "公开",
                    value: "public",
                },
            ],
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
