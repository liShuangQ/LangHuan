export interface PagesMenu {
    path: string; //菜单就随便写，路由就对应路由地址
    title: string;
    faPath: string[];
    icon?: string; //element的icon
    children?: PagesMenu[];
}

const menu: PagesMenu[] = [
    {
        path: "/pages/home",
        faPath: [],
        title: "主页",
        icon: "HomeFilled",
    },
    {
        path: "/pages/aiUtopia",
        faPath: [],
        title: "斯坦福小镇",
        icon: "Menu",
        children: [
            {
                title: "多角色对话",
                path: "/pages/aiUtopia/aiChat",
                faPath: ["aiUtopia"],
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
                title: "在用提示词",
                path: "/pages/prompt/usePrompt",
                faPath: ["rag"],
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
                title: "添加向量文件",
                path: "/pages/rag/addFile",
                faPath: ["rag"],
            },
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
        ],
    },
    {
        path: "tool",
        faPath: [],
        title: "Tool管理",
        icon: "Menu",
        children: [
            {
                title: "工具组管理",
                path: "/pages/tool/functionGroup",
                faPath: ["tool"],
            },
            {
                title: "工具管理",
                path: "/pages/tool/function",
                faPath: ["tool"],
            },
        ],
    },
    {
        path: "user",
        faPath: [],
        title: "系统管理",
        icon: "Menu",
        children: [
            {
                title: "路由查看",
                path: "/pages/systemManagement/routeView",
                faPath: ["user"],
            },
            {
                title: "用户管理",
                path: "/pages/systemManagement/user",
                faPath: ["user"],
            },
            {
                title: "角色管理",
                path: "/pages/systemManagement/role",
                faPath: ["user"],
            },
            {
                title: "权限管理",
                path: "/pages/systemManagement/permission",
                faPath: ["user"],
            },
        ],
    },
    {
        path: "zhujian",
        faPath: [],
        title: "公共全局组件",
        icon: "Menu",
        children: [
            {
                title: "表格组件",
                path: "/pages/componentDemo/table",
                faPath: ["zhujian"],
            },
            {
                title: "表单组件",
                path: "/pages/componentDemo/form",
                faPath: ["zhujian"],
            },
        ],
    },
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
