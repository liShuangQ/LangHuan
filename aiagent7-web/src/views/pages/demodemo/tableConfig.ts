import {
    PaginationConfig,
    TableColumnConfig,
    TableConfig,
} from "@/components/globalComponents/ElementTableC/table-component";

export let tableColumnConfig = ref<TableColumnConfig[]>([
    {
        label: "ID",
        prop: "id",
    },
    {
        label: "方法名",
        prop: "methodName",
        width: "auto",
    },
    {
        label: "分类",
        prop: "category",
        width: "auto",
    },
    {
        label: "描述",
        prop: "description",
        width: "auto",
    },
    {
        label: "提示词",
        prop: "content",
        width: "auto",
    },
    {
        label: "创建时间",
        prop: "createdAt",
        width: "auto",
    },
    {
        label: "更新时间",
        prop: "updatedAt",
        width: "auto",
    },
    {
        label: "操作",
        prop: "buttonSlot",
        contentSlot: true,
        width: "170",
    },
]);
export let tableConfig = ref<TableConfig>({
    align: "center",
    stripe: true,
    border: true,
    tooltip: true,
    selection: false,
    expand: false,
    index: true,
    highlightCurrentRow: false,
    maxHeight: 600, // 一般不设置
    tableLayout: "fixed",
});
export let tableData = ref<any[]>([]);
export let paginationConfig = ref<PaginationConfig>({
    currentPage: 1,
    pageSize: 10,
    total: 0,
    background: true,
});
