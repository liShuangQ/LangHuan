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
        label: '文件名称',
        prop: 'fileName',
        width: 'auto',
    },
    {
        label: '文件类型',
        prop: 'fileType',
        width: 'auto',
    },
    {
        label: '文件大小',
        prop: 'fileSize',
        width: 'auto',
    },
    {
        label: '文档切割数',
        prop: 'documentNum',
        contentSlot: true,
        width: 'auto',
    },
    {
        label: '文件描述',
        prop: 'fileDesc',
        width: 'auto',
    },
    {
        label: '文件组ID',
        prop: 'fileGroupId',
        width: 'auto',
    },
    {
        label: '文件组',
        prop: 'fileGroupName',
        width: 'auto',
    },
    {
        label: '上传时间',
        prop: 'uploadedAt',
        width: 'auto',
    },
    {
        label: '创建人',
        prop: 'uploadedBy',
        width: 'auto',
    },
    {
        label: '操作',
        prop: 'buttonSlot',
        contentSlot: true,
        width: '180',
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