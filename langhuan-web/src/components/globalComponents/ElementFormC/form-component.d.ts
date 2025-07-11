export interface FormConfig {
    inline?: boolean;
    labelPosition?: "left" | "right" | "top";
    labelWidth?: string | number;
    /**
     * 表单域标签的后缀
     */
    labelSuffix?: string;
    /**
     * 是否隐藏必填字段标签旁边的红色星号。
     */
    hideRequiredAsterisk?: boolean;
    /**
     * 星号的位置。
     */
    requireAsteriskPosition?: "left" | "right";
    showMessage?: boolean;
    /**
     * 是否以行内形式展示校验信息
     */
    inlineMessage?: boolean;
    /**
     * 是否在输入框中显示校验结果反馈图标
     */
    statusIcon?: boolean;
    /**
     * 是否在 rules 属性改变后立即触发一次验证
     */
    validateOnRuleChange?: boolean;
    size?: "" | "large" | "default" | "small";
    /**
     * 是否禁用该表单内的所有组件。 如果设置为 true, 它将覆盖内部组件的 disabled 属性
     */
    disabled?: boolean;
    /**
     * 当校验失败时，滚动到第一个错误表单项
     */
    scrollToError?: boolean;
}

interface BaseConfig {
    type?: string;
    key: string;
    show?: boolean;
    width?: string | number;
    label?: string;
    value?: any;
    rule?: any;
    col?: number;
    placeholder?: string;
    disabled?: boolean;
    clearable?: boolean;
    size?: "large" | "small" | "default" | "";
    elem?:
        | "year"
        | "month"
        | "date"
        | "dates"
        | "datetime"
        | "week"
        | "datetimerange"
        | "daterange"
        | "monthrange";
}

interface InputConfig {
    /**
     * 在 formatter的情况下显示值，我们通常同时使用 parser
     */
    formatter?: Function;
    parser?: Function;
    /**
     * element的 show-password 属性，得到一个可切换显示隐藏的密码框
     */
    showPassword?: boolean;
    /**
     * 在输入框中（后）添加图标
     */
    suffixIcon?: string;
    /**
     * 在输入框中（前）添加图标
     */
    prefixIcon?: string;
    /**
     * 用于输入多行文本信息可缩放的输入框。
     */
    textarea?: boolean;
    /**
     * 配合textarea，设置文字输入类型的 autosize 属性使得根据内容自动调整的高度。 你可以给 autosize 提供一个包含有最大和最小高度的对象，让输入框自动调整。
     */
    autosize?: boolean | { minRows?: number; maxRows?: number };
    /**
     * 启用在输入框中前置一个元素的插槽，通常是标签或按钮。:name="'prepend-' + item.key",只对非 type="textarea" 有效
     */
    prepend?: boolean;
    /**
     * 启用在输入框中后置一个元素的插槽，通常是标签或按钮。:name="'prepend-' + item.key",只对非 type="textarea" 有效
     */
    append?: boolean;
    /**
     * 使用 maxlength 和 minlength 属性, 来控制输入内容的最大字数和最小字数。
     */
    maxlength?: string | number;
    /**
     * 使用 maxlength 和 minlength 属性, 来控制输入内容的最大字数和最小字数。
     */
    minlength?: string | number;
    /**
     * 允许你通过设置 show-word-limit 到 true 来显示剩余字数。
     */
    showWordLimit?: boolean;
    /**
     * 是否开启插槽
     */
    suffix?: boolean;
    /**
     * 输入建议对象中用于显示的键名
     */
    valueKey?: string;
    /**
     * 获取输入建议的防抖延时，单位为毫秒
     */
    debounce?: number;
    placement?:
        | "top"
        | "top- start"
        | "top-end"
        | "bottom"
        | "bottom-start"
        | "bottom-end";
    /**
     * step-strictly属性接受一个Boolean。 如果这个属性被设置为 true，则只能输入步进的倍数。
     */
    stepStrictly?: boolean;
    /**
     * 设置 precision 属性可以控制数值精度，接收一个 Number
     */
    precision?: number;
    /**
     * 控制按钮位置
     */
    controlsPosition?: "" | "right";
}

interface SelectConfig {
    option?: {
        value: any;
        label: string;
        disabled?: boolean;
    }[];
    /**
     * 你可以为选项进行分组来区分不同的选项,它的 label 属性为分组名
     */
    optionGroup?: {
        label: string;
        option: {
            value: string;
            label: string;
            disabled?: boolean;
        }[];
    }[];
    /**
     * 为 el-select 设置 multiple 属性即可启用多选， 此时 v-model 的值为当前选中值所组成的数组。 默认情况下选中值会以 Tag 组件的形式展现。
     */
    multiple?: boolean;
    /**
     * 可以设置 collapse-tags 属性将它们合并为一段文字。
     */
    collapseTags?: boolean;
    /**
     * 您可以使用 collapse-tags-tooltip 属性来启用鼠标悬停折叠文字以显示具体所选值的行为。
     */
    collapseTagsTooltip?: boolean;
    /**
     * 为el-select添加filterable属性即可启用搜索功能。 默认情况下，Select 会找出所有 label 属性包含输入值的选项。
     */
    filterable?: boolean;
    /**
     * 创建并选中未包含在初始选项中的条目。
     */
    allowCreate?: boolean;
    /**
     * 设置自定义option的插槽，插槽传出item
     * <template v-if="item.optionCustom">
     *     <slot :item="optionItem" :name="'optionCustom-' + item.key"></slot>
     * </template>
     */
    optionCustom?: boolean;
    min?: number;
    max?: number;
    button?: boolean;
    border?: boolean;
    /**
     * 自定义slider的返回显示。 :format-tooltip="(value:number)=>formatTooltip(value)"
     */
    formatTooltip?: Function;
    /**
     * 改变step的值可以改变步长， 通过设置 show-stops 属性可以显示间断点
     */
    showStops?: boolean;
    /**
     * 通过输入框输入来改变当前的值。设置 show-input 属性会在右侧显示一个输入框
     */
    showInput?: boolean;
    /**
     * 配置 range 属性以激活范围选择模式，该属性的绑定值是一个数组，由最小边界值和最大边界值组成。
     */
    range?: boolean;
    /**
     * 设置 marks 属性可以在滑块上显示标记。
     */
    marks?: Object;
    /**
     * 使用active-text属性与inactive-text属性来设置开关的文字描述。
     */
    activeText?: string;
    /**
     * 使用active-text属性与inactive-text属性来设置开关的文字描述。
     */
    inactiveText?: string;
    /**
     * 使用 inline-prompt 属性来控制文本是否显示在点内。
     */
    inlinePrompt?: boolean;
    /**
     * 开关的颜色onColor offColor
     */
    onColor?: string;
    offColor?: string;
    /**
     * 设置 active-value 和 inactive-value 属性， 它们接受 Boolean、String 或 Number 类型的值。
     */
    activeValue?: boolean | string | number;
    /**
     * 设置 active-value 和 inactive-value 属性， 它们接受 Boolean、String 或 Number 类型的值。
     */
    inactiveValue?: boolean | string | number;
    loading?: boolean;
    /**
     * 其中的选项是否从服务器远程加载
     */
    remote?: boolean;
        /**
     * remote-method为一个Function，它会在输入值发生变化时调用，参数为当前输入值。 需要注意的是，如果 el-option 是通过 v-for 指令渲染出来的，此时需要为 el-option 添加 key 属性， 且其值需具有唯一性，比如这个例子中的 item.value。
     */
    remoteMethod?: Function;
}

interface TimeSelectConfig {
    /**
     * 文本框可输入
     */
    editable?: boolean;
    /**
     * 显示在输入框中的格式
     */
    format?: string;
    /**
     * 选择范围时的分隔符
     */
    rangeSeparator?: string;
    valueFormat?: string;
    /**
     * 在范围选择器里取消两个日期面板之间的联动
     */
    unlinkPanels?: boolean;
    /**
     * 一个用来判断该日期是否被禁用的函数，接受一个 Date 对象作为参数。 应该返回一个 Boolean 值。
     */
    disabledDate?: Function;
    startPlaceholder?: string;
    endPlaceholder?: string;
    dateFormat?: string;
    timeFormat?: string;
    /**
     * 是否为时间范围选择
     */
    isRange?: boolean;
    /**
     *    是否使用箭头进行时间选择
     */
    arrowControl?: boolean;
    start?: string;
    step?: string | number;
    end?: string;
    colors?: string[];
    /**
     * 为组件设置 show-text 属性会在右侧显示辅助文字。 通过设置 texts 可以为每一个分值指定对应的辅助文字。 texts 为一个数组，长度应等于最大值 max。
     */
    texts?: string[];
    /**
     * 属性 allow-half 允许出现半星
     */
    allowHalf?: boolean;
    icons?: string;
    voidIcon?: string;
    /**
     * 是否显示当前分数， show-score 和 show-text 不能同时为真
     */
    showScore?: boolean;
    /**
     * 是否显示辅助文字，若为真，则会从 texts 数组中选取当前分数对应的文字内容
     */
    showText?: boolean;
    textColor?: string;
    scoreTemplate?: string;
}

export type FormItemConfig = BaseConfig &
    InputConfig &
    SelectConfig &
    TimeSelectConfig;
export type FormItemConfigs = FormItemConfig[][];

export interface FormDefineExpose {
    /**
     * @Author: shuangqi.li
     * @Description 查询表单的绑定值
     * @param {} 无
     * @return {} 无
     */
    getFromValue: () => Object;
    /**
     * @Author: shuangqi.li
     * @Description 热修改表单配置
     * @param {options} 修改的数组
     * @return {void} 无
     */
    setFormOption: (options: FormItemConfig[]) => void;
    /**
     * @Author: shuangqi.li
     * @Description
     * @param {} 无
     * @return {} 返回Promise<unknown>根据.then .catch确定是否校验通过
     */
    submitForm: () => Promise<unknown>;
    /**
     * @Author: shuangqi.li
     * @Description 重置表单
     * @param {} 无
     * @return {} 无
     */
    resetForm: () => void;
}
