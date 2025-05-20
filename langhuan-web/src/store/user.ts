import { defineStore } from "pinia";
import { store } from "@/utils";
import router from "@/router";
import menuData, { PagesMenu } from "@/router/menu";
import menu from "@/store/menu";
import { http } from "@/plugins/axios";

export default defineStore("user", {
    state: (): { info: any | null; isAdmin: boolean } => {
        return {
            info: null,
            isAdmin: false,
        };
    },
    actions: {
        //设置用户信息
        setUserInfo(data: object) {
            this.info = data;
        },
        //清除用户信息
        clearUserInfo() {
            this.info = null;
        },
        //登录
        async userLogin(userData: object) {
            await http
                .request<any>({
                    url: "/user/login",
                    method: "post",
                    q_spinning: true,
                    q_contentType: "json",
                    data: userData,
                })
                .then(async (res: any) => {
                    if (res.code === 200) {
                        ElMessage.success("登陆成功");
                        const token = res.data.token;
                        store.set(process.env.TOKEN_KEY as string, {
                            expire: 60 * 24 * 24,
                            token,
                        });
                        await router.push({ path: "/" });
                    }
                })
                .catch((err) => {
                    ElMessage.error(err);
                    console.log(err);
                });
        },
        //获取用户信息
        async getUserInfo(token: string) {
            // menu().setMenu(menuData as PagesMenu[])
            // this.setUserInfo({})
            // return
            await http
                .request<any>({
                    url: "/user/getUserInfoByToken",
                    method: "post",
                    q_spinning: true,
                    data: {
                        token: token,
                    },
                })
                .then(async (res: any) => {
                    if (res.code === 200) {
                        if (process.env.AFTER_MENU === "true") {
                            menu().setMenu(res.menu as PagesMenu[]);
                        } else {
                            menu().setMenu(menuData as PagesMenu[]);
                        }
                        this.setUserInfo(res.data);
                        const roles = toRaw(this.info)?.role ?? [];
                        // HACK 暂时这样判断是否是管理员
                        if (
                            roles.some((e: any) => e.id === 1 || e.id === "1")
                        ) {
                            this.isAdmin = true;
                        } else {
                            this.isAdmin = false;
                        }
                    }
                })
                .catch((err) => {
                    menu().setMenu(menuData as PagesMenu[]);
                    console.log(err);
                });
        },
        //前端登出
        async userLogOut() {
            await http
                .request<any>({
                    url: "/user/logout",
                    method: "get",
                    q_spinning: true,
                    data: {},
                })
                .then(async (res: any) => {
                    if (res.code === 200) {
                        this.info = null;
                        localStorage.removeItem(
                            process.env.TOKEN_KEY as string
                        );
                        router.push({ name: "login" });
                    }
                })
                .catch((err) => {
                    console.log(err);
                });
        },
    },
});
