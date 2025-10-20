import { reactive } from "vue";
import { GlobalVars } from "../../../types/globalVars";

const globalVars = reactive<GlobalVars>({
    demo: false,
})
export default globalVars