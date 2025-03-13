import user from "@/store/user";

export interface IData {
    expire?: number;

    [k: string]: any;
}

// token 模拟自动删除
export default {
    set(k: string, d: IData): void {
        if (d.expire) {
            d.expire = new Date().getTime() + d.expire * 1000 * 60;
        }
        localStorage.setItem(k, JSON.stringify(d));
    },
    get(k: string): IData | null {
        const item = localStorage.getItem(k);
        if (item) {
            const data = JSON.parse(item);
            const expire = data?.expire;
            if (expire && expire < new Date().getTime()) {
                const userStore = user();
                userStore.userLogOut();
                return null;
            }
            return data;
        }
        return null;
    },
    token() {
        return this.get(process.env.TOKEN_KEY as string)?.token;
    },
};
