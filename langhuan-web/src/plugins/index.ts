import {App} from "vue";
import setupPinia from "./pinia";
import autoRegisterComponents from "./globalComponents";
import setMdDditor from "./mdDditor";
export function setupPlugins(app: App): void {
    setMdDditor(app);
    setupPinia(app);
    autoRegisterComponents(app);
}
