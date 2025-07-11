<template>
    <div class="overflow-y-scroll">
        <div class="h-[700px]">
            <ElementTableC
                ref="tableComRef"
                :paginationConfig="paginationConfig"
                :tableColumnConfig="tableColumnConfig"
                :tableConfig="tableConfig"
                :tableData="tableData"
                @handle="tableHandle"
                @indexMethod="indexMethod"
                @tableRowClassName="tableRowClassName"
            >
                <template #expand="props">
                    {{ props.row }}
                    <div m="4">
                        <p m="t-0 b-2">State: state</p>
                        <p m="t-0 b-2">City: city</p>
                    </div>
                </template>
                <template #header-data6> 自定义插槽</template>
                <template #content-data6="scope"> {{ scope.row }}</template>

                <template #content-data8="scope">
                    <el-button size="small" @click="editFun(scope.row)">编辑开关
                    </el-button>
                </template>
            </ElementTableC>
        </div>
        <div style="margin-top: 20px">
            <el-button
                @click="
                    tableComRef!.toggleSelection([tableData[1], tableData[2]])
                "
            >控制选中
            </el-button>

            <el-button @click="tableComRef!.toggleSelection()"
            >清除选中
            </el-button
            >

            <el-button @click="tableComRef!.tableMethod('clearSelection')">
                清除选中2
            </el-button>
        </div>
        <div style="margin-top: 20px">
            <el-button @click="tableComRef!.resetDateFilter(['data5'])"
            >清除筛选
            </el-button
            >
            <el-button @click="tdadd()">添加数据</el-button>
            <el-button @click="pageChange()">改变页码</el-button>
        </div>
    </div>
</template>

<script lang="ts">
export default {
    auto: true,
};
</script>
<script lang="ts" setup>
import {TableDefineExpose} from "@/components/globalComponents/ElementTableC/table-component";
import {
    paginationConfig,
    tableColumnConfig,
    tableConfig,
    tableData,
} from "@/views/pages/componentDemo/table/config";

const tableComRef = ref<TableDefineExpose>();
let editProps = {}
// 获取用于编辑控制的变量
nextTick(() => {
    editProps = tableComRef.value!.getEditProps()
})
const tdadd = () => {
    let itemD = {}
    for (let i = 1; i <= 8; i++) {
        if (i === 1) {
            itemD['data' + i] = new Date()
        } else if (i === 5) {
            itemD['data' + i] = Math.random() < 0.5 ? 1 : 2
        } else {
            itemD['data' + i] = '演示数据' + new Date().getTime()
        }
    }
    // 添加用于编辑控制的变量
    tableData.value.push({...itemD, ...editProps});
    paginationConfig.value.total = tableData.value.length;
};
const pageChange = () => {
    paginationConfig.value.currentPage = 3;
    paginationConfig.value.background = !paginationConfig.value.background;
    console.log(paginationConfig, "paginationConfig");
};
const editFun = (row: any) => {
    Object.keys(editProps).forEach((k: string) => {
        row[k] = !row[k]
    })
};

// 注意和stripe会冲突
const tableRowClassName = (
    e: {
        row: any;
        rowIndex: number;
    },
    fn: any
) => {
    // if (e.rowIndex === 0) {
    //     return fn('success-row')
    // }
    // if (e.rowIndex === 2) {
    //     return fn('warning-row')
    // }
    // return fn('')
};

const tableHandle = (t: string, d: any, key: string) => {
    console.log("tableHandle:::", t, d, key);
};
const indexMethod = (index: number, fn: Function) => {
    fn('-' + index)
};
</script>

<style scoped></style>
