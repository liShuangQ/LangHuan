export interface TableConfig {
    /**
     * 功能区的对齐方式，默认center
     */
    align?: 'left' | 'center' | 'right'
    /**
     * stripe 可以创建带斑马纹的表格。 如果 true, 表格将会带有斑马纹。
     */
    stripe?: boolean
    /**
     * 默认情况下，Table 组件是不具有竖直方向的边框的， 如果需要，可以使用 border 属性，把该属性设置为 true 即可启用。
     */
    border?: boolean
    /**
     * 纵向内容过多时，可选择固定表头。只要在 el-table 元素中定义了 height 属性，即可实现固定表头的表格，而不需要额外的代码。
     */
    // height?: number | string
    /**
     * 通过设置 max-height 属性为 el-table 指定最大高度。 此时若表格所需的高度大于最大高度，则会显示一个滚动条。
     */
    maxHeight?: number | string
    /**
     * show-overflow-tooltip属性。 默认情况下，如果单元格内容过长，会占用多行显示。 若需要单行显示可以使用 show-overflow-tooltip 属性，它接受一个 Boolean， 为 true 时多余的内容会在 hover 时以 tooltip 的形式显示出来。
     */
    tooltip?: boolean
    /**
     * 实现多选
     */
    selection?: boolean
    /**
     * 实现单选
     */
    highlightCurrentRow?: boolean
    /**
     * 通过设置 type="expand" 和 slot 可以开启展开行功能， el-table-column 的模板会被渲染成为展开行的内容，展开行可访问的属性与使用自定义列模板时的 slot 相同。
     * <template #expand">
     * </template>
     */
    expand?: Boolean
    /**
     * 通过给 type=index 的列传入 index 属性，可以自定义索引。 该属性传入数字时，将作为索引的起始值。 也可以传入一个方法，它提供当前行的行号（从 0 开始）作为参数，返回值将作为索引展示。
     */
    index?: Boolean
    /**
     * 通过属性 table-layout 可以指定表格中单元格、行和列的布局方式
     *
     */
    tableLayout?: 'auto' | 'fixed'
    /**
     * 表格尺寸
     */
    size?: 'large' | 'default' | 'small'
}

export interface TableColumnConfig {
    /**
     * prop 属性来对应对象中的键名即可填入数据
     */
    prop: string
    /**
     * 用 label 属性来定义表格的列名
     */
    label: string
    /**
     * 可以使用 width 属性来定义列宽
     */
    width?: number | string
    /**
     * 固定列需要使用 fixed 属性，它接受 Boolean 值。 如果为 true, 列将被左侧固定. 它还接受传入字符串，left 或 right，表示左边固定还是右边固定。
     */
    fixed?: boolean | 'left' | 'right'
    /**
     * 数据结构比较复杂的时候，可使用多级表头来展现数据的层次关系。
     * 只需要将el-table-column 放置于el-table-column 中，你可以实现组头。
     */
    children?: TableColumnConfig[]
    /**
     * 在列中设置 sortable 属性即可实现以该列为基准的排序， 接受一个 Boolean，默认为 false。 可以通过 Table 的 default-sort 属性设置默认的排序列和排序顺序。 可以使用 sort-method 或者 sort-by 使用自定义的排序规则。 如果需要后端排序，需将 sortable 设置为 custom，同时在 Table 上监听 sort-change 事件， 在事件回调中可以获取当前排序的字段名和排序顺序，从而向接口请求排序后的表格数据。 在本例中，我们还使用了 formatter 属性，它用于格式化指定列的值， 接受一个 Function，会传入两个参数：row 和 column， 可以根据自己的需求进行处理。
     */
    sortable?: boolean
    /**
     * 是否开启表头的自定义
     * <template #header-${TableColumnConfig.prop}> 自定义表头</template>
     */
    headerSlot?: boolean
    /**
     * 是否开启表格数据的自定义
     * <template #content-${TableColumnConfig.prop}="scope"> {{ scope.row }} </template>
     */
    contentSlot?: boolean
    /**
     * filters 是一个数组，决定可筛选的选项
     */
    filters?: { text: string, value: string }[]
    /**
     * filter-method 是一个方法，它用于决定某些数据是否显示， 会传入三个参数：value, row 和 column。
     */
    filterMethod?: Function
    /**
     * 对齐方式，默认center
     */
    align?: 'left' | 'center' | 'right'
    /**
     * 表头对齐方式，默认center
     */
    headerAlign?: 'left' | 'center' | 'right'
    /**
     * 表格中的数据是否可编辑，目前只可支持编辑框为input框，编辑后直接修改表格数据
     * 需要在表格数据中（tableData）的每个map中添加用于控制编辑状态的变量，可通过getEditProps方法获取{...,...[xxxref].getEditProps()}
     * 后手动控制每个key的布尔值。key一般为[${prop} + 'EEdit']，默认布尔值为false
     */
    isEdit?: boolean
    /**
     * 表格中的数据是否为按钮，通过handle方法暴露
     */
    isLink?: 'primary' | 'success' | 'warning' | 'danger' | 'info'

}

export interface PaginationConfig {
    currentPage: number
    pageSize: number
    total: number
    background?: boolean
    disabled?: boolean
    small?: boolean
    layout?: string
}

export interface TableDefineExpose {
    /**
     * 获取编辑列的的用于控制编辑框的编辑值
     */
    getEditProps: () => Object
    /**
     * 多选时控制选中信息，rows不传入时全部去除
     * @param rows
     */
    toggleSelection: (rows?: any[]) => void
    /**
     * 清除筛选信息，columnKeys不传入时全部去除
     * @param columnKeys
     */
    resetDateFilter: (columnKeys?: string[] | undefined) => void
    /**
     * 表格的方法，用于操作表格组件的ref方法调用
     *
     * clearSelection	用于多选表格，清空用户的选择
     *
     * getSelectionRows	返回当前选中的行
     *
     * toggleRowSelection	用于多选表格，切换某一行的选中状态， 如果使用了第二个参数，则可直接设置这一行选中与否。row, selected
     *
     * toggleAllSelection	用于多选表格，切换全选和全不选
     *
     * toggleRowExpansion	用于可扩展的表格或树表格，如果某行被扩展，则切换。 使用第二个参数，您可以直接设置该行应该被扩展或折叠。row, expanded
     *
     * setCurrentRow	用于单选表格，设定某一行为选中行， 如果调用时不加参数，则会取消目前高亮行的选中状态。	row
     *
     * clearSort	用于清空排序条件，数据会恢复成未排序的状态
     *
     * clearFilter	传入由columnKey 组成的数组以清除指定列的过滤条件。 如果没有参数，清除所有过滤器	columnKeys
     *
     * doLayout	对 Table 进行重新布局。 当表格可见性变化时，您可能需要调用此方法以获得正确的布局
     *
     * sort	手动排序表格。 参数 prop 属性指定排序列，order 指定排序顺序。	prop: string, order: string
     *
     * scrollTo	滚动到一组特定坐标	(options: ScrollToOptions | number, yCoord?: number)
     *
     * setScrollTop	设置垂直滚动位置	top
     *
     * setScrollLeft 设置水平滚动位置	left
     * @param methodName 方法名
     * @param d 参数
     */
    tableMethod: (methodName: string, ...d: any) => void

}