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
            key: "groupName",
            type: "input",
            label: "文件组名称",
            col: 12,
            clearable: true,
        },
        {
            value: "",
            key: "groupType",
            type: "input",
            label: "文件组类型",
            col: 12,
            clearable: true,
        },
        {
            value: "",
            key: "groupDesc",
            type: "input",
            label: "文件组描述",
            textarea:true,
            col: 24,
            clearable: true,
        },
    ],
];
