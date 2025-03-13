import {
    PaginationConfig,
    TableColumnConfig,
    TableConfig
} from "@/components/globalComponents/ElementTableC/table-component";

export let tableColumnConfig = ref<TableColumnConfig[]>([
    {
        label: '排序&编辑',
        prop: 'data1',
        width: 200,
        sortable: true,
        isEdit: true
    },
    {
        label: '多级表格',
        prop: 'data2',
        width: 500,
        children: [
            {
                label: '表格编辑',
                prop: 'data3',
                isEdit: true
            },
            {
                label: '二级表格',
                prop: 'data4',
                children: [
                    {
                        label: '排序&过滤',
                        prop: 'data5',
                        sortable: true,
                        filters: [
                            {text: '1', value: '1'},
                            {text: '2', value: '2'},
                        ],
                        filterMethod: (value: string, row: any, column: any) => {
                            const property = column['property']
                            return row[property].toString() === value.toString()
                        }
                    },
                    {
                        label: '',
                        prop: 'data6',
                        headerSlot: true,
                        contentSlot: true,
                    },
                    {
                        label: '是否按钮',
                        prop: 'data7',
                        isLink: 'success',
                    }
                ]
            },
        ]
    },
    {
        label: '表头3',
        prop: 'data8',
        width: 'auto',
        contentSlot: true,
    }
])
export let tableConfig = ref<TableConfig>({
    align:'center',
    stripe: true,
    border: false,
    tooltip: true,
    selection: true,
    expand: true,
    index: true,
    highlightCurrentRow: true,
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