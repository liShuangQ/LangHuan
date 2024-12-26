import {App} from "vue";
import setupPinia from "./pinia";
import autoRegisterComponents from "./globalComponents";

export function setupPlugins(app: App): void {
    setupPinia(app);
    autoRegisterComponents(app);
}
