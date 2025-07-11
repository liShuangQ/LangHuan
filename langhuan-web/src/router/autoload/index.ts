import {RouteRecordRaw} from "vue-router";
import {PagesRouters} from "@/router/routes";

const layoutsFun = (require as any).context('../../layouts', false, /\.vue$/, 'sync');
let layouts: {} = {}
layoutsFun.keys().forEach((k: string) => {
    layouts[k] = layoutsFun(k)
})

const viewsFun = (require as any).context('../../views', true, /\index.vue$/, 'sync');
let views: {} = {}
viewsFun.keys().forEach((k: string) => {
    views[k] = viewsFun(k)
})


function getRoutes() {
    const layoutRoutes = [] as RouteRecordRaw[];
    Object.entries(layouts).forEach(([file, module]) => {
        const route = getRouteByModule(file, module as any, '1');

        route["children"] = getChildrenRoutes(route);
        layoutRoutes.push(route);
    });
    return layoutRoutes;
}

function getChildrenRoutes(layoutRoute: RouteRecordRaw) {
    const routes = [] as RouteRecordRaw[];
    Object.entries(views).forEach(([file, module]) => {

        if (file.includes(`${layoutRoute.name as string}`)) {
            const route = getRouteByModule(file, module as any, '2');
            route && routes.push(route);
        }

    });

    return routes;
}

function getRouteByModule(file: string, module: { [key: string]: any }, type: string) {
    const path: string = file.replace(/^\.|\/index|\.vue/gi, "");
    if (type === '1') {
        const router: RouteRecordRaw = {
            name: path.replace(/^\//, '').replace(/\//g, '.'),
            path: path,
            component: module.default,
        } as RouteRecordRaw;
        return Object.assign(router, module.default.route || {});
    }
    if (type === '2') {
        const moduleDefault = module.default
        if (moduleDefault?.auto) {
            const router: RouteRecordRaw = {
                name: path.replace(/^\//, '').replace(/\//g, '.'),
                path: path,
                component: () => import(`../../views${path}/index.vue`),
            } as RouteRecordRaw;

            // HACK: 如果想在菜单搜索中出现，注意 path 加上 pages/xxx 的关键字特征
            //自定义路由: export default {
            //   route: { path: "/user" },
            // };
            return Object.assign(router, module.default.route || {});
        } else {
            return null
        }
    }
}

const routes = getRoutes()
routes.forEach(e => {
    if (e.name === 'pages') {
        e.children && e.children.push(...PagesRouters)
    }
})
export default routes;
