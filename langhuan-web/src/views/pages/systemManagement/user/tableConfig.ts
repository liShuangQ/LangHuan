import {
    PaginationConfig,
    TableColumnConfig,
    TableConfig
} from "@/components/globalComponents/ElementTableC/table-component";

export let tableColumnConfig = ref<TableColumnConfig[]>([
    {
        label: '姓名',
        prop: 'name',
    },
    {
        label: '用户名',
        prop: 'username',
        width: 'auto',
    },
    {
        label: '手机号',
        prop: 'phone',
        width: 'auto',
    },
    {
        label: '性别',
        prop: 'gender',
        width: 'auto',
    },
    {
        label: '是否启用', //（0-未启用；1-启用中）
        prop: 'enabled',
        width: 'auto',
    },
    {
        label: '创建时间',
        prop: 'formatCreationTime',
        width: 'auto',
    },
    {
        label: '上一次登录时间',
        prop: 'formatLastLoginTime',
        width: 'auto',
    },
    {
        label: '操作',
        prop: 'buttonSlot',
        contentSlot: true,
        width: '200',
    }
])
export let tableConfig = ref<TableConfig>({
    align: 'center',
    stripe: true,
    border: true,
    tooltip: true,
    selection: false,
    expand: false,
    index: true,
    highlightCurrentRow: false,
    maxHeight: 600,  // 一般不设置
    tableLayout: 'fixed'
})
export let tableData = ref<any[]>([])
export let paginationConfig = ref<PaginationConfig>({
    currentPage: 1,
    pageSize: 20,
    total: 0,
    background: true,
})