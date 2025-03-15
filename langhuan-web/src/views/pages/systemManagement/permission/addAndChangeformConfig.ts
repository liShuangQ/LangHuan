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
            label: "ID",
            col: 24,
            disabled: true,
        },
        {
            value: "",
            key: "name",
            type: "input",
            label: "名字",
            col: 24,
            rule: [
                { required: true, message: "请输入用户", trigger: "blur" },
            ],
            clearable: true,
        },
        {
            value: "",
            key: "url",
            type: "input",
            label: "权限标识",
            col: 24,
            rule: [
                { required: true, message: "请输入权限标识", trigger: "blur" },
            ],
            clearable: true,
        },
        {
            value: "",
            key: "parentId",
            type: "input",
            label: "父级ID",
            col: 24,
            rule: [
                { required: true, message: "请输入父级ID", trigger: "blur" },
            ],
            clearable: true,
        },
    ],
];
