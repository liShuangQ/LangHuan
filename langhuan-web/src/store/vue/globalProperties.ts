import { App } from "vue";

// const { proxy } = getCurrentInstance() as any;
// onMounted(() => {
//   console.log(proxy.msg);
// });
const setGlobalProperties = (app:App) => {
    app.config.globalProperties.msg = 'hello'
}
export default setGlobalProperties