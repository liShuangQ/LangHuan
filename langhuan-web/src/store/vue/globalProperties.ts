import { App } from "vue";


const setGlobalProperties = (app:App) => {
    app.config.globalProperties.msg = 'hello'
}
export default setGlobalProperties