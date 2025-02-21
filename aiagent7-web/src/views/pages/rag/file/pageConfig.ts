export default {
    addUrl: "/rag/file-group/add",
    deleteUrl: "/rag/file-group/delete",
    updataUrl: "/rag/file-group/update",
    searchUrl: "/rag/file-group/query",
    search_dayTransformation: ["createdAt"], // 是否有时间转换的字段
    search_tableData_key: "records", // 表格数据， 从res.data 开始算
    search_paginationConfig_key: "total", // 总数数据 ，从res.data 开始算
    relation: false, // 是否开启关联  打开后 addAndChangeFormShowFun【relation】和relevancyShowFun 自行修改
    relationBtnName: "关联文件组", // 是否开启关联
};
