
export default {
    set(d: string): void {
        localStorage.setItem(process.env.TOKEN_KEY as string, d);
    },
    token() {
        return localStorage.getItem(process.env.TOKEN_KEY as string);
    },
};
