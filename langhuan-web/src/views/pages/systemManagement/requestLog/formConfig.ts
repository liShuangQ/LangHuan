import { FormConfig, FormItemConfigs } from "@/components/globalComponents/ElementFormC/form-component"

export let formConfig: FormConfig = {
    size: 'default',
    showMessage: true
}

export let formItemConfig: FormItemConfigs = [
    [
        {
            value: '',
            key: 'apiName',
            type: 'input',
            label: '接口名称',
            col: 6,
            clearable: true,
            placeholder: '请输入接口名称'
        },
        {
            value: '',
            key: 'username',
            type: 'input',
            label: '用户名',
            col: 6,
            clearable: true,
            placeholder: '请输入用户名'
        },
        {
            col: 12,
            key: "button",
            type: "custom",
        },
    ],
    // [
    //     {
    //         value: '',
    //         key: 'httpMethod',
    //         type: 'select',
    //         label: 'HTTP方法',
    //         col: 6,
    //         clearable: true,
    //         placeholder: '请选择HTTP方法',
    //         option: [
    //             { value: 'GET', label: 'GET' },
    //             { value: 'POST', label: 'POST' },
    //             { value: 'PUT', label: 'PUT' },
    //             { value: 'DELETE', label: 'DELETE' },
    //             { value: 'PATCH', label: 'PATCH' }
    //         ]
    //     },
    //     {
    //         value: '',
    //         key: 'isSuccess',
    //         type: 'select',
    //         label: '执行状态',
    //         col: 6,
    //         clearable: true,
    //         placeholder: '请选择执行状态',
    //         option: [
    //             { value: true, label: '成功' },
    //             { value: false, label: '失败' }
    //         ]
    //     }
    // ],
    // [
    //     {
    //         value: [],
    //         key: 'timeRange',
    //         type: 'datePicker',
    //         label: '创建时间',
    //         elem: 'datetimerange',
    //         col: 12,
    //         format: "YYYY-MM-DD HH:mm:ss",
    //         valueFormat: "YYYY-MM-DD HH:mm:ss",
    //         placeholder: '请选择时间范围',
    //         rangeSeparator: '至',
    //         startPlaceholder: '开始时间',
    //         endPlaceholder: '结束时间'
    //     }
    // ]
]