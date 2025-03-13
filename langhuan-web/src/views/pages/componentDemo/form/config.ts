import {FormConfig, FormItemConfigs} from "@/components/globalComponents/ElementFormC/form-component"

export let formConfig: FormConfig = {
    size: 'default',
    showMessage: true
}
export let formItemConfig: FormItemConfigs = [
    [
        {
            value: '',
            key: 'inputkey',
            type: 'input',
            label: 'input测试',
            rule: [
                {required: true, message: 'Please input Activity name', trigger: 'blur'},
                {min: 3, max: 5, message: 'Length should be 3 to 5', trigger: 'blur'},
            ],
            col: 12,
            disabled: false,
            clearable: true,
            showPassword: false,
            prepend: true,
            append: true,
            size: 'large',
            maxlength: '10',
            showWordLimit: true,
            // formatter: (value:string)=>{
            //     return `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
            // },
            // parser: (value:string)=>{
            //     return  value.replace(/\$\s?|(,*)/g, '')
            // },
            // suffixIcon:'Calendar',
            // prefixIcon:'Search',
            // textarea:true,
            // autosize:true,
        },
        {
            col: 12,
            key: 'customkey',
            type: 'custom',
        },

    ],
    [
        {
            value: '',
            key: 'autocompletekey',
            type: 'autocomplete',
            label: 'autocomplete测试',
            suffix: true,
            clearable: true,
            valueKey: 'value'
        }
    ],
    [
        {
            value: ['1'],
            key: 'checkboxkey',
            type: 'checkbox',
            label: 'checkbox测试',
            option: [
                {value: '1', label: '1-1'},
                {value: '2', label: '2-2'},
                {value: '3', label: '3-3', disabled: true},
                {value: '4', label: '4-4'},
            ],
            min: 1, max: 2,
            button: false,
            border: true
        }
    ],
    [
        {
            value: ["2024-02-07 17:09:16", "2024-02-16 20:48:09"],
            key: 'datePickerkey',
            type: 'datePicker',
            label: 'datePicker测试',
            elem: 'datetimerange',
            col: 12,
            editable: true,
            unlinkPanels: false,
            format: "YYYY-MM-DD HH:mm:ss",
            valueFormat: "YYYY-MM-DD HH:mm:ss",
            dateFormat: "YYYY/MM/DD ddd",
            timeFormat: "A hh:mm:ss",
            disabledDate: (time: Date) => {
                return time.getTime() > Date.now()
            }
        }
    ],
    [
        {
            value: '',
            key: 'timePickerkey',
            type: 'timePicker',
            label: 'timePicker测试',
            format: "HH:mm:ss",
            col: 12,
            arrowControl: true
        }
    ],
    [
        {
            value: '08:45 AM',
            key: 'timeSelect',
            type: 'timeSelect',
            label: 'timeSelect测试',
            format: "hh:mm A",
            start: "08:30",
            step: "00:15",
            end: "18:30",
            col: 12,
        }
    ],
    [
        {
            value: [],
            key: 'selectkey',
            type: 'select',
            label: 'select测试',
            rule: [
                {
                    required: true,
                    message: 'Please select Activity zone',
                    trigger: 'change',
                },
            ],
            col: 12,
            multiple: true,
            optionCustom: true,
            optionGroup: [
                {
                    label: 'A组',
                    option: [
                        {
                            value: '1',
                            label: "标题1"
                        },
                        {
                            value: '2',
                            label: "标题2"
                        }
                    ]
                },
                {
                    label: 'B组',
                    option: [
                        {
                            value: '3',
                            label: "标题3"
                        }
                    ]
                }
            ],
            // option: [
            //     {
            //         value: '1',
            //         label: "标题1"
            //     },
            //     {
            //         value: '2',
            //         label: "标题2"
            //     }
            // ]
        },
    ],
    [
        {
            value: 0,
            col: 12,
            key: 'inputNumberkey',
            type: 'inputNumber',
            label: 'inputNumber测试',
            step: 0.2,
            stepStrictly: true,
            precision: 2,
            controlsPosition: 'right'
        },
    ],
    [
        {
            value: '1',
            key: 'radiokey',
            type: 'radio',
            label: 'radio测试',
            option: [
                {value: '1', label: '1-1'},
                {value: '2', label: '2-2'},
                {value: '3', label: '3-3', disabled: true},
                {value: '4', label: '4-4'},
            ],
            min: 1, max: 2,
            button: true,
            border: false
        }
    ],
    [
        {
            value: 1,
            key: 'ratekey',
            type: 'rate',
            label: 'rate测试',
            allowHalf: true,
            showScore: true,
            showText: false,
            textColor: '#ff9900',
            scoreTemplate: 'star',
            voidIcon: "ChatRound",
            colors: ['#409eff', '#67c23a', '#FF9900']
        }
    ],
    [
        {
            value: [10, 60],
            key: 'sliderkey',
            type: 'slider',
            label: 'slider测试',
            col: 12,
            formatTooltip: (val: number) => {
                return val + '%'
            },
            showInput: true,
            showStops: true,
            range: true,
            step: 5,
            marks: {
                0: '0°C',
                8: '8°C',
                37: '37°C',
                50: {
                    style: {
                        color: '#1989FA',
                    },
                    label: '50%',
                },
            }
        }
    ],
    [
        {
            value: false,
            key: 'switchkey',
            type: 'switch',
            label: 'switch测试',
            col: 12,
            activeText: '关',
            inactiveText: '开',
            // inlinePrompt: true,
        }
    ],
]