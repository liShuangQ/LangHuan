<template>
    <el-table-column
        :align="columnConfig.align || 'center'"
        :column-key="columnConfig.prop"
        :filter-method="(columnConfig.filterMethod as any)"
        :filters="columnConfig.filters"
        :header-align="columnConfig.headerAlign || 'center'"
        :label="columnConfig.label"
        :prop="columnConfig.prop"
        :show-overflow-tooltip="props.tooltip"
        :sortable="columnConfig.sortable || false"
        :width="columnConfig.width || ''"
    >
        <template
            v-if="columnConfig.children && columnConfig.children.length > 0"
        >
            <sub-column
                v-for="item in columnConfig.children"
                :key="item.prop"
                :columnConfig="item"
                :tooltip="props.tooltip"
                :fa-emit="props.faEmit"
                :fa-slots="props.faSlots"
            >
                <template v-for="(val , code) in props.faSlots" #[code]="scope" :key="code">
                    <slot :name="code" v-bind="{...scope}"></slot>
                </template>
            </sub-column>
        </template>


        <template v-if="columnConfig.headerSlot || false" #header>
            <slot :name="`header-${columnConfig.prop}`"></slot>
        </template>

        <template v-if="columnConfig.contentSlot || false" #default="scope">
            <slot :name="`content-${columnConfig.prop}`" v-bind="{...scope}"></slot>
        </template>

        <template v-if="columnConfig.isLink || false" #default="scope">
            <el-button
                :type="columnConfig.isLink || 'primary'"
                link
                @click="faEmit('handle','link',toRaw(scope.row),columnConfig.prop)"
            >{{ scope.row[columnConfig.prop] }}
            </el-button>
        </template>
        <!-- XXX: 可能会小概率引发和自身的data变量的key冲突 -->
        <template v-if="columnConfig.isEdit || false" #default="scope">
            <el-input
                v-if="scope.row[columnConfig.prop + 'EEdit']"
                v-model="scope.row[columnConfig.prop]"
                :size="'default'"
                placeholder=""
            />
            <span v-else>{{ scope.row[columnConfig.prop] }}</span>
        </template>
    </el-table-column>
</template>

<script lang="ts" setup>
import {TableColumnConfig} from "./table-component";
import {toRaw} from 'vue'

const props = defineProps<{
    columnConfig: TableColumnConfig;
    faSlots: any;
    tooltip?: boolean;
    faEmit?: any
}>();

</script>

<style scoped></style>
