import { createPinia } from "pinia";
import { App } from "vue";
import {setupIcons} from "@/plugins/elementPlusIcon";

export default function setupPinia(app: App) {
    app.use(createPinia());
    setupIcons(app)
}
