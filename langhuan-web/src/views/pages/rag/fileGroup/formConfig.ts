import {FormConfig, FormItemConfigs} from "@/components/globalComponents/ElementFormC/form-component"

export let formConfig: FormConfig = {
    size: 'default',
    labelWidth: '90px',
    labelPosition: 'left',
    showMessage: true
}
export let formItemConfig: FormItemConfigs = [
    [
        {
            value: '',
            key: 'groupName',
            type: 'input',
            label: '文件组名称',
            col: 5,
            clearable: true,
        },
        {
            value: '',
            key: 'groupType',
            type: 'input',
            label: '文件组类型',
            col: 5,
            clearable: true,
        },
        {
            col: 14,
            key: 'button',
            type: 'custom',
        },
    ],
]