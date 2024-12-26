import {RouteRecordRaw} from "vue-router";
import {view_Routes} from "@/router";
import {PagesMenu} from "@/router/menu";
import {cloneDeep} from "lodash";

/**
 * 过滤pages布局路由传过来的菜单信息
 * 将收到的菜单根据path变换为string[]，根据这个数组对比path筛选出路由中多余的，在守卫中根据name删除
 * @param menu
 */
export function fifterPagesRouter(menu: any): RouteRecordRaw[] {
    const flatMenu: PagesMenu[] = []

    function deep(d: PagesMenu[]): void {
        d.forEach((ele: PagesMenu): void => {
            if (!ele.children || ele.children.length === 0) {
                flatMenu.push(ele);
                return;
            } else {
                ele.children && deep(ele.children);
            }
        })
    }

    cloneDeep(menu).forEach((e: PagesMenu): void => {
        if (!e.children || e.children.length === 0) {
            flatMenu.push(e)
        } else {
            deep(e.children)
        }
    })
    const flatMenuKey: string[] = flatMenu.map((e: PagesMenu) => e.path)
    const page: RouteRecordRaw[] | undefined = view_Routes.find((e: RouteRecordRaw): boolean => e.name === 'pages')!.children
    return page?.filter((e: RouteRecordRaw) => !flatMenuKey.includes(e.path)) as RouteRecordRaw[]
}