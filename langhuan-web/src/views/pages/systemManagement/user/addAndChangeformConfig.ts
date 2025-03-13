import {FormConfig, FormItemConfigs} from "@/components/globalComponents/ElementFormC/form-component"

export let addAndChangeFormConfig: FormConfig = {
    size: 'default',
    labelWidth: '70px',
    labelPosition: 'top',
    showMessage: true
}

export let addAndChangeFormItemConfig: FormItemConfigs = [
    [
        {
            value: '',
            key: 'name',
            type: 'input',
            label: '姓名',
            col: 12,
            clearable: true,
        },
        {
            value: '',
            key: 'username',
            type: 'input',
            label: '用户名',
            col: 12,
            rule: [
                {required: true, message: '请输入用户名', trigger: 'blur'},
            ],
            clearable: true,
        },
        {
            value: '',
            key: 'password',
            type: 'input',
            label: '密码',
            showPassword: true,
            col: 12,
            rule: [
                {required: true, message: '请输入密码', trigger: 'blur'},
            ],
            clearable: true,
        },
        {
            value: '',
            key: 'phone',
            type: 'input',
            label: '手机号',
            col: 12,
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
            col: 12,
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
            col: 12,
            clearable: true,
        },
    ],
]