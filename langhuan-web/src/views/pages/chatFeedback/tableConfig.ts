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
        label: "用户",
        prop: "userId",
        width: "auto",
    },
    {
        label: "用户扩展信息",
        prop: "userInfo",
        width: "auto",
    },
    {
        label: "问题唯一标识",
        prop: "questionId",
        width: "auto",
    },
    {
        label: "问题内容",
        prop: "questionContent",
        width: "auto",
    },
    {
        label: "回答内容",
        prop: "answerContent",
        width: "auto",
    },
    {
        label: "互动类型",
        prop: "interaction",
        width: "auto",
    },
    {
        label: "互动时间",
        prop: "interactionTime",
        width: "auto",
    },
    {
        label: "调用的知识库",
        prop: "knowledgeBaseIds",
        contentSlot: true,
        width: "auto",
    },
    {
        label: "附加建议",
        prop: "suggestion",
        width: "auto",
    },
    // {
    //     label: "操作",
    //     prop: "buttonSlot",
    //     contentSlot: true,
    //     width: "170",
    // },
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
