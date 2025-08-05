export interface PagesMenu {
    path: string; //菜单就随便写，路由就对应路由地址
    title: string;
    faPath: string[];
    icon?: string; //element的icon
    children?: PagesMenu[];
}

const menu: PagesMenu[] = [
    {
        path: "/admin",
        faPath: [],
        title: "仪表盘",
        icon: "HomeFilled",
    },
    {
        path: "chats",
        faPath: [],
        title: "对话窗口",
        icon: "Menu",
        children: [
            {
                title: "对话",
                path: "/pages/chat",
                faPath: ["chats"],
            },
            {
                title: "对话-旧",
                path: "/pages/homeNext",
                faPath: ["chats"],
            },
        ],
    },
    {
        path: "prompt",
        faPath: [],
        title: "提示词管理",
        icon: "Menu",
        children: [
            {
                title: "提示词优化",
                path: "/pages/prompt/promptOptimize",
                faPath: ["prompt"],
            },
            {
                title: "在用提示词",
                path: "/pages/prompt/usePrompt",
                faPath: ["prompt"],
            },
        ],
    },
    {
        path: "rag",
        faPath: [],
        title: "RAG管理",
        icon: "Menu",
        children: [
            {
                title: "文件组管理",
                path: "/pages/rag/fileGroup",
                faPath: ["rag"],
            },
            {
                title: "文件管理",
                path: "/pages/rag/file",
                faPath: ["rag"],
            },
            {
                title: "添加文件",
                path: "/pages/rag/addRagDocuments",
                faPath: ["rag"],
            },
            // {
            //     title: "添加文字",
            //     path: "/pages/rag/addDocuments",
            //     faPath: ["rag"],
            // },
            {
                title: "召回标记",
                path: "/pages/rag/recallTesting",
                faPath: ["rag"],
            },
        ],
    },
    // {
    //     path: "tool",
    //     faPath: [],
    //     title: "Tool管理",
    //     icon: "Menu",
    //     children: [
    //         {
    //             title: "工具组管理",
    //             path: "/pages/tool/functionGroup",
    //             faPath: ["tool"],
    //         },
    //         {
    //             title: "工具管理",
    //             path: "/pages/tool/function",
    //             faPath: ["tool"],
    //         },
    //     ],
    // },
    {
        path: "dailyOperation",
        faPath: [],
        title: "日常运维",
        icon: "Menu",
        children: [
            {
                title: "对话反馈",
                path: "/pages/chatFeedback",
                faPath: ["dailyOperation"],
            },
            {
                title: "系统通知",
                path: "/pages/systemManagement/message",
                faPath: ["dailyOperation"],
            },
            {
                title: "操作日志",
                path: "/pages/systemManagement/requestLog",
                faPath: ["dailyOperation"],
            },
        ],
    },
    {
        path: "system",
        faPath: [],
        title: "系统管理",
        icon: "Menu",
        children: [
            {
                title: "路由查看",
                path: "/pages/systemManagement/routeView",
                faPath: ["system"],
            },
            // {
            //     title: "系统通知呈现",
            //     path: "/pages/systemManagement/messageShow",
            //     faPath: ["system"],
            // },
            {
                title: "用户管理",
                path: "/pages/systemManagement/user",
                faPath: ["system"],
            },
            {
                title: "角色管理",
                path: "/pages/systemManagement/role",
                faPath: ["system"],
            },
            {
                title: "权限管理",
                path: "/pages/systemManagement/permission",
                faPath: ["system"],
            },
        ],
    },
    // {
    //     path: "zhujian",
    //     faPath: [],
    //     title: "公共全局组件",
    //     icon: "Menu",
    //     children: [
    //         {
    //             title: "表格组件",
    //             path: "/pages/componentDemo/table",
    //             faPath: ["zhujian"],
    //         },
    //         {
    //             title: "表单组件",
    //             path: "/pages/componentDemo/form",
    //             faPath: ["zhujian"],
    //         },
    //     ],
    // },
];

function setMenu(menu: PagesMenu[]) {
    menu.forEach((item) => {
        if (item.children) {
            item.children.forEach((child) => {
                child.faPath = item.path ? [item.path] : [];
                if (child.children) {
                    setMenu(child.children);
                }
            });
        }
    });
    return menu;
}

export default setMenu(menu);
