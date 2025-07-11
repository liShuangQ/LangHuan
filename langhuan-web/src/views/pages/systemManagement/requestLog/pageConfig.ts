// 接口日志管理页面配置
export const pageConfig = {
    title: '接口调用日志',
    description: '查看和管理系统接口调用日志记录',

    // 表格配置
    table: {
        showSelection: true,
        showExpand: true,
        showIndex: true,
        defaultPageSize: 20,
        pageSizes: [10, 20, 50, 100]
    },

    // 查询表单配置
    searchForm: {
        showReset: true,
        showSearch: true,
        layout: 'inline'
    },

    // 操作按钮配置
    actions: {
        showBatchDelete: true,
        showCleanHistory: true,
        showExport: false,
        showImport: false
    },

    // 权限配置
    permissions: {
        view: 'api:log:view',
        delete: 'api:log:delete',
        batchDelete: 'api:log:batchDelete',
        clean: 'api:log:clean'
    }
}

// 统计配置
export const statisticsConfig = {
    showTotalCount: true,
    showSuccessRate: true,
    showAvgResponseTime: true,
    refreshInterval: 60000 // 60秒刷新一次
}

// 对话框配置
export const dialogConfig = {
    detail: {
        width: '800px',
        showFullScreen: true
    },
    clean: {
        width: '400px',
        defaultDays: 30,
        maxDays: 365
    }
}