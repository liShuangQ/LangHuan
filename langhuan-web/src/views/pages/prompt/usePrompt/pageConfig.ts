export default {
    addUrl: "/prompts/usePrompt/add",
    deleteUrl: "/prompts/usePrompt/delete",
    updataUrl: "/prompts/usePrompt/update",
    searchUrl: "/prompts/usePrompt/query",
    search_dayTransformation: ["createdAt","updatedAt"], // 是否有时间转换的字段
    search_tableData_key: "records", // 表格数据， 从res.data 开始算
    search_paginationConfig_key: "total", // 总数数据 ，从res.data 开始算
};
