import {
    FormConfig,
    FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";
import { http } from "@/plugins/axios";

export let addAndChangeForm: FormConfig = {
    size: "default",
    labelWidth: "100px",
    labelPosition: "top",
    showMessage: true,
};

export let addAndChangeFormItem: FormItemConfigs = [
    [
        {
            value: "",
            key: "title",
            type: "input",
            textarea: true,
            label: "通知标题",
            col: 24,
            clearable: true,
            placeholder: "请输入通知标题",
            autosize: { minRows: 1, maxRows: 2 },
            rule: [
                { required: true, message: "请输入通知标题", trigger: "blur" },
                {
                    min: 1,
                    max: 100,
                    message: "标题长度在 1 到 100 个字符",
                    trigger: "blur",
                },
            ],
        },
    ],
    [
        {
            value: "",
            key: "content",
            type: "input",
            textarea: true,
            label: "通知内容",
            col: 24,
            placeholder: "请输入通知内容",
            autosize: { minRows: 3, maxRows: 4 },
            rule: [
                { required: true, message: "请输入通知内容", trigger: "blur" },
                {
                    min: 1,
                    max: 1000,
                    message: "内容长度在 1 到 1000 个字符",
                    trigger: "blur",
                },
            ],
        },
    ],
    [
        {
            value: "info",
            key: "notificationLevel",
            type: "select",
            label: "通知级别",
            col: 12,
            placeholder: "请选择通知级别",
            rule: [
                {
                    required: true,
                    message: "请选择通知级别",
                    trigger: "change",
                },
            ],
            option: [
                { label: "紧急 (Critical)", value: "critical" },
                { label: "错误 (Error)", value: "error" },
                { label: "警告 (Warning)", value: "warning" },
                { label: "信息 (Info)", value: "info" },
            ],
        },
        {
            value: "system",
            key: "notificationType",
            type: "select",
            label: "通知类型",
            col: 12,
            placeholder: "请选择通知类型",
            rule: [
                {
                    required: true,
                    message: "请选择通知类型",
                    trigger: "change",
                },
            ],
            option: [
                { label: "系统通知", value: "system" },
                { label: "提醒通知", value: "reminder" },
                { label: "警报通知", value: "alert" },
                { label: "消息通知", value: "message" },
                { label: "更新通知", value: "update" },
            ],
        },
    ],
    [
        {
            value: "",
            key: "userIds",
            type: "select",
            option:[],
            multiple: true,
            filterable: true,
            label: "目标用户",
            placeholder: "目标用户（为空则全局通知）",
            col: 24,
        },
    ],
    [
        {
            value: "",
            key: "referenceId",
            type: "input",
            label: "引用ID",
            col: 12,
            clearable: true,
            placeholder: "关联的业务对象ID（可选）",
        },
        {
            value: "",
            key: "referenceType",
            type: "input",
            label: "引用类型",
            col: 12,
            clearable: true,
            placeholder: "关联的业务对象类型（可选）",
        },
    ],
    [
        {
            value: "",
            key: "expiresAt",
            type: "datePicker",
            elem: "datetime",
            label: "过期时间",
            col: 24,
            placeholder: "请选择过期时间",
            format: "YYYY-MM-DD HH:mm:ss",
            valueFormat: "YYYY-MM-DD HH:mm:ss",
            rule: [
                { required: true, message: "请输入过期时间", trigger: "change" },
            ],
        },
    ],
];
