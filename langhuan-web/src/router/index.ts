import { App } from "vue";
import { createRouter, createWebHashHistory } from "vue-router"; //createWebHistory
import { routes } from "./routes";
import viewRoutes from "./autoload/index";
import guard from "@/router/guard";
const router = createRouter({
    // history: createWebHistory(),
    history: createWebHashHistory(),
    routes: [...routes,...viewRoutes],
});
export const setupRouter = async (app: App) => {
    guard(router);
    app.use(router);
};
export const view_Routes = viewRoutes
export default router;
