import { reactive } from "vue";
import { GlobalVars } from "../../../types/globalVars";
// const globalVars = inject<GlobalVars>("globalVars");
// watch(
//   () => globalVars?.demo,
//   (newValue) => {
//     console.log(newValue, "全局监听");
//   }
// );
const globalVars = reactive<GlobalVars>({
    demo: false,
})
export default globalVars