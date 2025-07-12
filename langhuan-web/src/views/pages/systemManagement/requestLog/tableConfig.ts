import {
    PaginationConfig,
    TableColumnConfig,
    TableConfig
} from "@/components/globalComponents/ElementTableC/table-component";

export let tableColumnConfig = ref<TableColumnConfig[]>([
    // {
    //     label: 'ID',
    //     prop: 'id',
    //     width: 80,
    //     sortable: true
    // },
    {
        label: '接口信息',
        prop: 'apiInfo',
        width: 300,
        children: [
            {
                label: '接口名称',
                prop: 'apiName',
            },
            {
                label: 'HTTP方法',
                prop: 'httpMethod',
                contentSlot: true
            },
            {
                label: '接口URL',
                prop: 'apiUrl',
            }
        ]
    },
    {
        label: '用户信息',
        prop: 'userInfo',
        width: 200,
        children: [
            {
                label: '用户名',
                prop: 'userName',
            },
            {
                label: '请求IP',
                prop: 'requestIp',
            }
        ]
    },
    {
        label: '执行信息',
        prop: 'executionInfo',
        width: 250,
        children: [
            {
                label: '执行时长(ms)',
                prop: 'executionTime',
                contentSlot: true
            },
            {
                label: '执行状态',
                prop: 'isSuccess',
                contentSlot: true,
                filters: [
                    {text: '成功', value: 'true'},
                    {text: '失败', value: 'false'},
                ],
                filterMethod: (value: boolean, row: any, column: any) => {
                    const property = column['property']
                    return row[property] === value
                }
            }
        ]
    },
    {
        label: '请求时间',
        prop: 'createTime',
        sortable: true,
        contentSlot: true
    },
    // {
    //     label: '操作',
    //     prop: 'actions',
    //     width: 150,
    //     fixed: 'right',
    //     contentSlot: true,
    // }
])

export let tableConfig = ref<TableConfig>({
    align: 'center',
    stripe: true,
    border: true,
    tooltip: true,
    selection: true,
    expand: true,
    index: true,
    highlightCurrentRow: true,
    maxHeight: 600,
    tableLayout: 'fixed'
})

export let tableData = ref<any[]>([])

export let paginationConfig = ref<PaginationConfig>({
    currentPage: 1,
    pageSize: 20,
    total: 0,
    background: true,
})