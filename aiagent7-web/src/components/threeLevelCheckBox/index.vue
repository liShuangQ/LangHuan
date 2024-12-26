<template>
    <div>
        <div class="flex gap-20">
            <div>左左 左左 左左</div>
            <div>右右 右右 右右</div>
        </div>
        <div v-for="(item,index) in All" :key="index" class="mb-20 bg-gray-200">
            <el-checkbox
                v-model="item.checkAll"
                @change="(val) => handleCheckAllChangeOne(val,item)"
            >{{ item.name }}
            </el-checkbox
            >
            <div v-for="(item2,index2) in item.children" :key="index2" class="flex gap-28">
                <el-checkbox
                    v-model="item2.checkAll"
                    @change="(val) => handleCheckAllChangeTwo(val,item,item2)"
                >{{ item2.name }}
                </el-checkbox
                >
                <el-checkbox-group
                    v-model="item2.value"
                    @change="(val) => handleCheckedCitiesChange(val,item,item2)">
                    <el-checkbox v-for="(ele,ind) in item2.option" :key="ind" :label="ele">{{
                            ele
                        }}
                    </el-checkbox>
                </el-checkbox-group>
            </div>

        </div>
    </div>
</template>

<script lang="ts" setup>
const All = ref<any>(
    [
        {
            name: '0',
            checkAll: false,
            children: [
                {
                    name: '1-1',
                    checkAll: false,
                    option: ['1', '2', '3'],
                    value: []
                },
                {
                    name: '1-2',
                    checkAll: false,
                    option: ['1', '2', '3'],
                    value: []
                }
            ]
        }
    ]
)


const handleCheckAllChangeOne = (val: boolean, item: any) => {
    if (val) {
        item.children.forEach((e: any) => {
            e.checkAll = true
            e.value = e.option
        })
    } else {
        item.children.forEach((e: any) => {
            e.checkAll = false
            e.value = []
        })
    }
}
const handleCheckAllChangeTwo = (val: boolean, item: any, item2: any) => {
    item.checkAll = item.children.some((e: any) => e.checkAll)
    if (val) {
        item2.value = item2.option
    } else {
        item2.value = []
    }
}
const handleCheckedCitiesChange = (value: string[], item: any, item2: any) => {
    item2.checkAll = value.length >= 1
    item.checkAll = item.children.some((e: any) => e.checkAll)
}
</script>
