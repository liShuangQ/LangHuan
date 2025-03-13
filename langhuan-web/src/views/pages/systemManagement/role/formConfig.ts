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
            label: '名字',
            col: 7,
            clearable: true,
        },
        {
            value: '',
            key: 'remark',
            type: 'input',
            label: '备注',
            col: 7,
            clearable: true,
        },
        {
            col: 10,
            key: 'button',
            type: 'custom',
        },
    ],
]