import {
  FormConfig,
  FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";

export let formConfig: FormConfig = {
  size: "default",
  labelWidth: "80px",
  labelPosition: "left",
  showMessage: true,
};

export let formItemConfig: FormItemConfigs = [
  [
    {
      value: "",
      key: "username",
      type: "input",
      label: "用户名",
      col: 6,
      clearable: true,
      placeholder: "请输入用户名",
    },
    {
      value: "",
      key: "notificationLevel",
      type: "select",
      label: "通知级别",
      col: 6,
      clearable: true,
      placeholder: "请选择通知级别",
      option: [
        { label: "紧急", value: "critical" },
        { label: "错误", value: "error" },
        { label: "警告", value: "warning" },
        { label: "信息", value: "info" },
      ],
    },
    {
      value: "",
      key: "notificationType",
      type: "select",
      label: "通知类型",
      col: 6,
      clearable: true,
      placeholder: "请选择通知类型",
      option: [
        { label: "系统通知", value: "system" },
        { label: "提醒通知", value: "reminder" },
        { label: "警报通知", value: "alert" },
        { label: "消息通知", value: "message" },
        { label: "更新通知", value: "update" },
      ],
    },
    {
      value: "",
      key: "isRead",
      type: "select",
      label: "已读状态",
      col: 6,
      clearable: true,
      placeholder: "请选择已读状态",
      option: [
        { label: "未读", value: false },
        { label: "已读", value: true },
      ],
    },
  ],
  [
    {
      value: "",
      key: "isArchived",
      type: "select",
      label: "归档状态",
      col: 6,
      clearable: true,
      placeholder: "请选择归档状态",
      option: [
        { label: "未归档", value: false },
        { label: "已归档", value: true },
      ],
    },
    {
        col: 18,
        key: 'button',
        type: 'custom',
    },
  ],
];