import {App} from "vue";
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'



export function setupElementAndIcons(app: App) {
    app.use(ElementPlus, {
        locale: zhCn,
    })
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
        app.component(key, component)
    }
}
