<!---->
<!-- liShuangQ-->
<!-- XXX：不同类型的表单形式，暂不考虑使用component is分离组件，认为存在多余变量和方法的开销，为了可维护性 下面要更加注意表单之间的代码分离-->
<!---->
<template>
    <el-form
        ref="formRef"
        :disabled="formConfig.disabled"
        :hide-required-asterisk="formConfig.hideRequiredAsterisk"
        :inline="formConfig.inline"
        :inline-message="formConfig.inlineMessage"
        :label-position="formConfig.labelPosition || 'right'"
        :label-suffix="formConfig.labelSuffix || ''"
        :label-width="formConfig.labelWidth || ''"
        :model="formModel"
        :require-asterisk-position="
            formConfig.requireAsteriskPosition || 'left'
        "
        :rules="formRule"
        :scroll-to-error="formConfig.scrollToError"
        :show-message="formConfig?.showMessage ?? true"
        :size="formConfig.size || 'default'"
        :status-icon="formConfig.statusIcon"
        :validate-on-rule-change="formConfig?.validateOnRuleChange ?? true"
        class="demo-ruleForm"
    >
        <!--        :gutter="20"-->
        <el-row v-for="(row, rowIndex) in formItemConfig" :key="rowIndex" :gutter="20">
            <el-col v-for="item in row" :key="item.key" :span="item.col" v-show="item?.show??true">
                <el-form-item
                    v-if="item.type !== 'custom'"
                    :label="item.label"
                    :prop="item.key"
                >
                    <!--                    autocomplete-->
                    <el-autocomplete
                        v-if="item.type === 'autocomplete'"
                        v-model="formModel[item.key]"
                        :clearable="item.clearable"
                        :debounce="item.debounce || 300"
                        :disabled="item.disabled"
                        :fetch-suggestions="(querystring:string,cb:any)=>emit('handle','autocomplete',item.key,querystring,cb)"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请输入' + item.label
                        "
                        :placement="(item.placement || 'bottom-start') as any"
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :value-key="item.valueKey || 'value'"
                        @select="(value:Object)=>emit('handle','select',item.key,toRaw(value),'')"
                    >
                        <template v-if="item.suffix" #suffix>
                            <slot :name="'suffix-' + item.key"></slot>
                        </template>
                    </el-autocomplete>
                    <!--                    checkbox-->
                    <el-checkbox-group
                        v-if="item.type === 'checkbox'"
                        v-model="formModel[item.key]"
                        :clearable="item.clearable"
                        :disabled="item.disabled"
                        :max="item.max"
                        :min="item.min"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请选择' + item.label
                        "
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        @change="(value:any)=>emit('handle','change',item.key,value,'')"
                    >
                        <template v-if="item.button || false">
                            <el-checkbox-button
                                v-for="optionItem in item.option"
                                :key="optionItem.value"
                                :disabled="optionItem.disabled"
                                :label="optionItem.value"
                            >
                                {{ optionItem.label }}
                            </el-checkbox-button>
                        </template>
                        <template v-else>
                            <el-checkbox
                                v-for="optionItem in item.option"
                                :key="optionItem.value"
                                :border="item.border"
                                :disabled="optionItem.disabled"
                                :label="optionItem.value"
                            >
                                {{ optionItem.label }}
                            </el-checkbox>
                        </template>
                    </el-checkbox-group>
                    <!--                    datePicker-->
                    <el-date-picker
                        v-if="item.type === 'datePicker'"
                        v-model="formModel[item.key]"
                        :clearable="item.clearable"
                        :date-format="item.dateFormat"
                        :disabled="item.disabled"
                        :disabled-date="(time: Date)=>item.disabledDate?item.disabledDate(time):null"
                        :editable="item.editable || false"
                        :end-placeholder="item.endPlaceholder || 'end'"
                        :format="item.format"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请选择' + item.label
                        "
                        :placement="item.placement || 'bottom-start'"
                        :range-separator="item.rangeSeparator || '-'"
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :start-placeholder="item.startPlaceholder || 'start'"
                        :time-format="item.timeFormat"
                        :type="item.elem || 'date'"
                        :unlink-panels="item.unlinkPanels || false"
                        :value-format="item.valueFormat || item.format"
                        @change="(value:string|number)=>emit('handle','change',item.key,value,'')"
                    >
                    </el-date-picker>
                    <!--                    input-->
                    <el-input
                        v-if="item.type === 'input'"
                        v-model="formModel[item.key]"
                        :autosize="(item.autosize as any) || false"
                        :clearable="item.clearable"
                        :disabled="item.disabled"
                        :formatter="
                            (value:string | number) =>
                                item.formatter ? item.formatter(value) : value
                        "
                        :maxlength="item.maxlength as any"
                        :minlength="item.minlength"
                        :parser="
                            (value:string | number) =>
                                item.parser ? item.parser(value) : value
                        "
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请输入' + item.label
                        "
                        :prefix-icon="item.prefixIcon || ''"
                        :show-word-limit="item.showWordLimit"
                        :showPassword="item.showPassword"
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :suffix-icon="item.suffixIcon || ''"
                        :type="item.textarea ? 'textarea' : 'text'"
                        @change="(value:string|number)=>emit('handle','change',item.key,value,'')"
                    >
                        <!-- XXX: formatter,parser 是否存在冗余性能问题？ -->
                        <template v-if="item.prepend" #prepend>
                            <slot :name="'prepend-' + item.key"></slot>
                        </template>
                        <template v-if="item.append" #append>
                            <slot :name="'append-' + item.key"></slot>
                        </template>
                    </el-input>
                    <!--                    inputNumber-->
                    <el-input-number
                        v-if="item.type === 'inputNumber'"
                        v-model="formModel[item.key]"
                        :clearable="item.clearable"
                        :controls-position="item.controlsPosition || ''"
                        :disabled="item.disabled"
                        :max="item.max"
                        :min="item.min"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请输入' + item.label
                        "
                        :placement="item.placement || 'bottom-start'"
                        :precision="item.precision"
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :step="(item.step || 1) as any"
                        :step-strictly="item.stepStrictly"
                        @change="(value:any)=>emit('handle','change',item.key,value,'')"
                    >
                    </el-input-number>
                    <!--                    radio-->
                    <el-radio-group
                        v-if="item.type === 'radio'"
                        v-model="formModel[item.key]"
                        :clearable="item.clearable"
                        :disabled="item.disabled"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请输入' + item.label
                        "
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        @change="(value:any)=>emit('handle','change',item.key,value,'')"
                    >
                        <template v-if="item.button || false">
                            <el-radio-button
                                v-for="optionItem in item.option"
                                :key="optionItem.value"
                                :disabled="optionItem.disabled"
                                :label="optionItem.value"
                            >
                                {{ optionItem.label }}
                            </el-radio-button>
                        </template>
                        <template v-else>
                            <el-radio
                                v-for="optionItem in item.option"
                                :key="optionItem.value"
                                :border="item.border"
                                :disabled="optionItem.disabled"
                                :label="optionItem.value"
                            >
                                {{ optionItem.label }}
                            </el-radio>
                        </template>
                    </el-radio-group>
                    <!--                    rate-->
                    <el-rate
                        v-if="item.type === 'rate'"
                        v-model="formModel[item.key]"
                        :allow-half="item.allowHalf"
                        :clearable="item.clearable"
                        :colors="
                            item.colors || ['#F7BA2A', '#F7BA2A', '#F7BA2A']
                        "
                        :disabled="item.disabled"
                        :icons="item.icons as any || [StarFilled, StarFilled, StarFilled]"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请输入' + item.label
                        "
                        :placement="item.placement || 'bottom-start'"
                        :scoreTemplate="
                            formModel[item.key] + item.scoreTemplate
                        "
                        :show-score="item.showScore"
                        :show-text="item.showText"
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :text-color="item.textColor"
                        :texts="item.texts || []"
                        :void-icon="item.voidIcon as any || Star"
                        @change="(value:string|number)=>emit('handle','change',item.key,value,'')"
                    >
                    </el-rate>
                    <!--                    select-->
                    <el-select
                        v-if="item.type === 'select'"
                        v-model="formModel[item.key]"
                        :allow-create="item.allowCreate"
                        :clearable="item.clearable"
                        :collapse-tags="item.collapseTags"
                        :collapse-tags-tooltip="item.collapseTagsTooltip"
                        :disabled="item.disabled"
                        :filterable="item.filterable"
                        :multiple="item.multiple"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请选择' + item.label
                        "
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || 'default'"
                        :remote="item.remote"
                        :remote-method="item.remoteMethod"
                        value-key="value"
                        @change="(value:string|number)=>emit('handle','change',item.key,value,'')"
                    >
                        <template
                            v-if="
                                (item.optionGroup &&
                                    item.optionGroup.length > 0) ||
                                false
                            "
                        >
                            <el-option-group
                                v-for="group in item.optionGroup"
                                :key="group.label"
                                :label="group.label"
                            >
                                <el-option
                                    v-for="optionItem in group.option"
                                    :key="optionItem.value"
                                    :label="optionItem.label"
                                    :value="optionItem.value"
                                >
                                    <template v-if="item.optionCustom">
                                        <slot
                                            :item="optionItem"
                                            :name="'optionCustom-' + item.key"
                                        ></slot>
                                    </template>
                                </el-option>
                            </el-option-group>
                        </template>
                        <template v-else>
                            <el-option
                                v-for="optionItem in item.option"
                                :key="optionItem.value"
                                :label="optionItem.label"
                                :value="optionItem.value"
                            >
                                <template v-if="item.optionCustom">
                                    <slot
                                        :item="optionItem"
                                        :name="'optionCustom-' + item.key"
                                    ></slot>
                                </template>
                            </el-option>
                        </template>
                    </el-select>
                    <!--                    slider-->
                    <el-slider
                        v-if="item.type === 'slider'"
                        v-model="formModel[item.key]"
                        :clearable="item.clearable"
                        :disabled="item.disabled"
                        :format-tooltip="(value:number)=>item.formatTooltip ? item.formatTooltip(value) : value "
                        :marks="(item.marks || null) as any"
                        :placement="(item.placement || 'bottom-start') as any"
                        :range="item.range"
                        :show-input="item.showInput"
                        :show-stops="item.showStops"
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :step="(item.step || 1) as any"
                        @change="(value:any)=>emit('handle','change',item.key,value,'')"
                    >
                    </el-slider>
                    <!--                    switch-->
                    <el-switch
                        v-if="item.type === 'switch'"
                        v-model="formModel[item.key]"
                        :active-text="item.activeText"
                        :active-value="item.activeValue || true"
                        :clearable="item.clearable"
                        :disabled="item.disabled"
                        :inactive-text="item.inactiveText"
                        :inactive-value="item.inactiveValue || false"
                        :inline-prompt="item.inlinePrompt"
                        :loading="item.loading"
                        :placement="item.placement || 'bottom-start'"
                        :size="item.size || formConfig.size || 'default'"
                        :style="{
                            width:(item.width || '100%'),
                            '--el-switch-on-color': item.onColor,
                            '--el-switch-off-color': item.offColor,
                        }"
                        @change="(value:any)=>emit('handle','change',item.key,value,'')"
                    >
                    </el-switch>
                    <!--                    timePicker-->
                    <el-time-picker
                        v-if="item.type === 'timePicker'"
                        v-model="formModel[item.key]"
                        :arrow-control="item.arrowControl"
                        :clearable="item.clearable"
                        :disabled="item.disabled"
                        :end="item.end"
                        :format="(item.format as any) || 'HH:mm:ss'"
                        :is-range="item.isRange"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请选择' + item.label
                        "
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :value-format="item.valueFormat || item.format"
                        @change="(value:string|number)=>emit('handle','change',item.key,value,'')"
                    >
                    </el-time-picker>
                    <!--                    timeSelect-->
                    <el-time-select
                        v-if="item.type === 'timeSelect'"
                        v-model="formModel[item.key]"
                        :clearable="item.clearable"
                        :disabled="item.disabled"
                        :end="item.end"
                        :format="(item.format as any) || 'HH:mm'"
                        :placeholder="
                            item.placeholder
                                ? item.placeholder
                                : '请选择' + item.label
                        "
                        :placement="item.placement || 'bottom-start'"
                        :style="{width:(item.width || '100%')}"
                        :size="item.size || formConfig.size || 'default'"
                        :start="item.start"
                        :step="item.step as any"
                        @change="(value:string|number)=>emit('handle','change',item.key,value,'')"
                    >
                    </el-time-select>
                </el-form-item>
                <!-- 自定义 -->
                <slot
                    v-if="item.type === 'custom'"
                    :name="'custom-' + item.key"
                ></slot>
            </el-col>
        </el-row>
    </el-form>
</template>

<script lang="ts" setup>
import type {FormInstance} from "element-plus";
import {FormConfig, FormDefineExpose, FormItemConfig,} from "@/components/globalComponents/ElementFormC/form-component";
import {toRaw} from "vue";
import {Star, StarFilled} from "@element-plus/icons-vue";

const props = withDefaults(
    defineProps<{
        formConfig: FormConfig;
        formItemConfig: FormItemConfig[][];
    }>(),
    {
        formConfig: () => {
            return {};
        },
        formItemConfig: () => {
            return [[]];
        },
    }
);
let emit = defineEmits<{
    (event: "handle", type: string, key: string, data: any, other: any): void;
}>();
const formRef = ref<FormInstance>();
const formItemConfig = ref<FormItemConfig[][]>(props.formItemConfig);
const formModel = ref<any>({});
const formRule = ref<any>({});
formItemConfig.value.forEach((row: FormItemConfig[]) => {
    row.forEach((item: FormItemConfig) => {
        // HACK 影响setFormOption方法。此时formConfig里面的value和rule是冗余的，但是为了避免不必要的开销，暂时不动
        formModel.value[item.key] = item?.value ?? "";
        formRule.value[item.key] = item?.rule ?? [];
    });
});

// ---外部调用方法---
const setFormOption = (options: FormItemConfig[]) => {
    options.forEach((item: FormItemConfig) => {
        for (let i = 0; i < formItemConfig.value.length; i++) {
            let fi = formItemConfig.value[i].findIndex(
                (formItem: FormItemConfig) => formItem.key === item.key
            );
            if (fi !== -1) {
                // HACK 根据是否单独管理的变量调整
                Object.hasOwn(item, "value") &&
                (formModel.value[item.key] = item.value);
                Object.hasOwn(item, "rule") &&
                (formRule.value[item.key] = item.rule);
                formItemConfig.value[i][fi] = {
                    ...formItemConfig.value[i][fi],
                    ...item,
                };
                break;
            }
        }
    });
};
const getFromValue = () => {
    return toRaw(formModel.value);
};
const submitForm = async () => {
    return new Promise(async (res, rej) => {
        await formRef.value!.validate((valid, fields) => {
            valid ? res(getFromValue()) : rej(fields);
        });
    });
};
const resetForm = () => {
    formRef.value!.resetFields();
};

defineExpose<FormDefineExpose>({
    getFromValue,
    setFormOption,
    submitForm,
    resetForm,
});
</script>
