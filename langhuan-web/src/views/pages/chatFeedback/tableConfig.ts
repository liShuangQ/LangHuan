import {
    PaginationConfig,
    TableColumnConfig,
    TableConfig,
} from "@/components/globalComponents/ElementTableC/table-component";

export let tableColumnConfig = ref<TableColumnConfig[]>([
    // {
    //     label: "ID",
    //     prop: "id",
    // },
    {
        label: "用户",
        prop: "userName",
        width: "auto",
    },

    // {
    //     label: "用户扩展信息",
    //     prop: "userInfo",
    //     width: "auto",
    // },
    // {
    //     label: "问题唯一标识",
    //     prop: "questionId",
    //     width: "auto",
    // },
    {
        label: "互动类型",
        prop: "interaction",
        width: "90px",
        contentSlot: true,
    },
    {
        label: "互动时间",
        prop: "interactionTime",
        width: "200px",
    },
    {
        label: "问题内容",
        prop: "questionContent",
        width: "200px",
    },
    {
        label: "回答内容",
        prop: "answerContent",
        width: "200px",
    },
    {
        label: "附加建议",
        prop: "suggestion",
        width: "400px",
    },
    {
        label: "使用提示词",
        prop: "usePrompt",
        width: "200px",
    },
    {
        label: "使用模型",
        prop: "useModel",
        width: "200px",
    },
    {
        label: "是否开启rerank",
        prop: "useRank",
        width: "200px",
    },
    {
        label: "使用文件组",
        prop: "useFileGroupName",
        width: "200px",
    },
    {
        label: "调用的知识库",
        prop: "knowledgeBaseIds",
        contentSlot: true,
        width: "120px",
    },
    {
        label: "操作",
        prop: "buttonSlot",
        contentSlot: true,
        width: "240",
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
    pageSize: 20,
    total: 0,
    background: true,
});
