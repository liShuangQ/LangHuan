import { defineStore } from "pinia";
import menuData, { PagesMenu } from "@/router/menu";
import { cloneDeep } from "lodash";
import menu from "@/router/menu";

export default defineStore("menu", {
    state: (): {
        menu: PagesMenu[] | null;
        flatMenu: PagesMenu[];
        history: PagesMenu[];
    } => {
        return {
            menu: null,
            flatMenu: [],
            history: [],
        };
    },
    actions: {
        setMenu(data: PagesMenu[]): void {
            this.history = [];
            this.menu = data;
            this.setFlatMenu();
        },
        getMenu(): PagesMenu[] | null {
            return this.menu;
        },
        setFlatMenu(): void {
            const flatMenu: PagesMenu[] = [];

            function deep(d: PagesMenu[]) {
                d.forEach((ele) => {
                    if (!ele.children || ele.children.length === 0) {
                        flatMenu.push(ele);
                        return;
                    } else {
                        flatMenu.push(ele);
                        ele.children && deep(ele.children);
                    }
                });
            }

            cloneDeep(this.menu as PagesMenu[]).forEach((e) => {
                if (!e.children || e.children.length === 0) {
                    flatMenu.push(e);
                } else {
                    flatMenu.push(e);
                    deep(e.children);
                }
            });
            this.flatMenu = flatMenu;
        },
        setHistoryAdd(data: PagesMenu): void {
            if (this.history.length >= 7) {
                this.history.splice(0, 1);
            }
            if (this.history.find((e) => e.path === data.path)) {
                return;
            }
            this.history.push(data);
        },
        setHistoryDel(data: PagesMenu): void {
            this.history = this.history.filter((e) => e.path !== data.path);
        },
        getHistory(): PagesMenu[] {
            return this.history;
        },
        handleHistoryMenu(
            selectedKeys: string,
            type: "left" | "right" | "now"
        ): void {
            if (type === "left") {
                let i = 0;
                for (let index = 0; index < this.history.length; index++) {
                    if (this.history[index].path === selectedKeys) {
                        i = index;
                        break;
                    }
                }
                this.history.splice(0, i);
            }
            if (type === "right") {
                let i = 0;
                for (let index = 0; index < this.history.length; index++) {
                    if (this.history[index].path === selectedKeys) {
                        i = index;
                        break;
                    }
                }
                this.history.splice(i + 1, this.history.length);
            }
            if (type === "now") {
                this.history = [
                    this.history.find((e) => {
                        return e.path === selectedKeys;
                    }) as PagesMenu,
                ];
            }
        },
    },
});
