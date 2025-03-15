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
            rule: [
                { required: true, message: "Please input", trigger: "blur" },
            ],
            col: 24,
            clearable: true,
        },
    ],
];
