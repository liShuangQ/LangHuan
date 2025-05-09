import {
    PaginationConfig,
    TableColumnConfig,
    TableConfig
} from "@/components/globalComponents/ElementTableC/table-component";

export let tableColumnConfig = ref<TableColumnConfig[]>([
    // {
    //     label: 'ID',
    //     prop: 'id',
    // },
    {
        label: '文件组名称',
        prop: 'groupName',
        width: 'auto',
    },
    {
        label: '文件组类型',
        prop: 'groupType',
        width: 'auto',
    },
    {
        label: '文件组描述',
        prop: 'groupDesc',
        width: 'auto',
    },
    {
        label: '创建用户',
        prop: 'createdBy',
        width: 'auto',
    },
    {
        label: '创建时间',
        prop: 'createdAt',
        width: 'auto',
    },
    {
        label: '操作',
        prop: 'buttonSlot',
        contentSlot: true,
        width: '120',
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
    pageSize: 10,
    total: 0,
    background: true,
})