import {createApp} from "vue";
import router, {setupRouter} from "./router/index";
import {setupPlugins} from "@/plugins";
import "./style/preflight.css"
import "./style/tailwind.css"
import 'element-plus/dist/index.css'
import "./style/element.scss"
import setGlobalProperties from "./store/vue/globalProperties";
import globalVars from "./store/vue/globalVars";
import setDirective from './store/vue/directive'
import App from "./App.vue";

const bootstrap = async () => {
    const app = createApp(App);
    // 挂app方式（不推荐）
    setGlobalProperties(app)
    // 依赖注入方式（推荐）
    app.provide('globalVars', globalVars)
    // 自定义全局指令
    setDirective(app)
    // -------------
    setupPlugins(app);
    await setupRouter(app);
    await router.isReady();
    app.mount("#app");
};


bootstrap();
