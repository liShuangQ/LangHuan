import {App} from "vue";
import setupPinia from "./pinia";
import autoRegisterComponents from "./globalComponents";
import setMdDditor from "./mdDditor";
import {setupElementAndIcons} from "@/plugins/elementPlus";

export function setupPlugins(app: App): void {
    setMdDditor(app);
    setupElementAndIcons(app)
    autoRegisterComponents(app);
    setupPinia(app);
}
