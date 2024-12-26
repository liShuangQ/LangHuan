import {FormConfig, FormItemConfigs} from "@/components/globalComponents/ElementFormC/form-component"

export let formConfig: FormConfig = {
    size: 'default',
    labelWidth: '70px',
    labelPosition: 'left',
    showMessage: true
}
export let formItemConfig: FormItemConfigs = [
    [
        {
            value: '',
            key: 'name',
            type: 'input',
            label: '姓名',
            col: 5,
            clearable: true,
        },
        {
            value: '',
            key: 'username',
            type: 'input',
            label: '用户名',
            col: 5,
            clearable: true,
        },
        {
            value: '',
            key: 'gender',
            type: 'select',
            option: [
                {
                    label: '男(1)',
                    value: '1',
                },
                {
                    label: '女(2)',
                    value: '2',
                },
            ],
            label: '性别',
            col: 5,
            clearable: true,
        },
        {
            value: '',
            key: 'enabled',
            type: 'select',
            option: [
                {
                    label: '未启用(0)',
                    value: '0',
                },
                {
                    label: '启用中(1)',
                    value: '1',
                },
            ],
            label: '是否启用',
            col: 5,
            clearable: true,
        },
        {
            col: 4,
            key: 'button',
            type: 'custom',
        },
    ],
]