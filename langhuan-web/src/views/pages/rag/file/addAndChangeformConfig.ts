import {
    FormConfig,
    FormItemConfigs,
} from "@/components/globalComponents/ElementFormC/form-component";
import { cloneDeep } from "lodash";
import { formItemConfig as addFileFrom } from "../addFile/addFileFormconfig";

export let addAndChangeFormConfig: FormConfig = {
    size: "default",
    labelWidth: "90px",
    labelPosition: "top",
    showMessage: true,
};
export let addAndChangeFormItemConfig: FormItemConfigs = cloneDeep(addFileFrom);
