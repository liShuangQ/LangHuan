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
            key: "userId",
            type: "input",
            label: "用户名",
            col: 7,
            clearable: true,
        },
        {
            value: "",
            key: "interaction",
            type: "select",
            label: "分类",
            option: [
                {
                    label: "like",
                    value: "like",
                },
                {
                    label: "dislike",
                    value: "dislike",
                },
            ],
            col: 7,
            clearable: true,
        },
        {
            col: 10,
            key: "button",
            type: "custom",
        },
    ],
];
