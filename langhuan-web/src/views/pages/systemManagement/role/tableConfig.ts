import {
    PaginationConfig,
    TableColumnConfig,
    TableConfig
} from "@/components/globalComponents/ElementTableC/table-component";

export let tableColumnConfig = ref<TableColumnConfig[]>([
    // {
    //     label: 'ID',
    //     prop: 'id',
    //     width: '120',
    // },
    {
        label: '名字',
        prop: 'name',
        width: 'auto',
    },
    {
        label: '备注',
        prop: 'remark',
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