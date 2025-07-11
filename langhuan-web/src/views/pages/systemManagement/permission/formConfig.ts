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
            key: "name",
            type: "input",
            label: "名字",
            col: 6,
            clearable: true,
        },
        {
            value: "",
            key: "url",
            type: "input",
            label: "权限标识",
            col: 6,
            clearable: true,
        },
        {
            value: "",
            key: "parentId",
            type: "input",
            label: "父级ID",
            col: 6,
            clearable: true,
        },
        {
            col: 6,
            key: "button",
            type: "custom",
        },
    ],
];
