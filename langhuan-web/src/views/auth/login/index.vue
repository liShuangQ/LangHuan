<template>
    <div style="
      background-color: #9921e8;
      background-image: linear-gradient(315deg, #9921e8 0%, #5f72be 74%);
    ">
        <div class="body-bg min-h-screen pt-12 md:pt-20 pb-6 px-2 md:px-0" style="font-family: 'Lato', sans-serif">
            <header class="max-w-lg mx-auto">
                <div v-if="BASE_PROJECT_LOGO_URL_BIG" class="flex justify-center align-center">
                    <img :src="BASE_PROJECT_LOGO_URL_BIG" class="h-20"></img>
                </div>
                <a v-else href="#">
                    <h1 class="text-4xl font-bold text-white text-center">{{ BASE_PROJECT_NAME }}</h1>
                </a>
            </header>

            <main class="bg-white max-w-lg mx-auto p-8 md:p-12 my-10 rounded-lg shadow-2xl">
                <section>
                    <h3 class="font-bold text-2xl">Welcome to {{ BASE_PROJECT_NAME }}</h3>
                    <p class="text-gray-600 pt-2">Sign in to your account.</p>
                </section>

                <section class="mt-10">
                    <div class="flex flex-col">
                        <div class="mb-6 pt-3 rounded bg-gray-200">
                            <label class="block text-gray-700 text-sm font-bold mb-2 ml-3">UserId</label>
                            <input type="text" placeholder="请输入账号"
                                class="bg-gray-200 rounded w-full text-gray-700 focus:outline-none border-b-4 border-gray-300 focus:border-purple-600 transition duration-500 px-3 pb-3"
                                v-model="userData.username" />
                        </div>
                        <div class="mb-6 pt-3 rounded bg-gray-200">
                            <label class="block text-gray-700 text-sm font-bold mb-2 ml-3"
                                for="password">Password</label>
                            <input type="password" placeholder="请输入密码"
                                class="bg-gray-200 rounded w-full text-gray-700 focus:outline-none border-b-4 border-gray-300 focus:border-purple-600 transition duration-500 px-3 pb-3"
                                v-model="userData.password" />
                        </div>
                        <!-- <div class="flex justify-end">
                            <a
                                href="#"
                                class="text-sm text-purple-600 hover:text-purple-700 hover:underline mb-6"
                            >Forgot your password?</a
                            >
                        </div> -->
                        <button
                            class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 rounded shadow-lg hover:shadow-xl transition duration-200"
                            @click="userLogin()">
                            Sign In
                        </button>
                    </div>
                </section>
            </main>

            <div class="max-w-lg mx-auto text-center mt-12 mb-6">
                <p class="text-white">
                    Don't have an account?
                    <el-button type="text" class="font-bold hover:underline" @click="openSignUp">Sign up</el-button>.
                </p>
            </div>
        </div>

        <el-dialog v-model="signUpVisible" title="注册" width="800">
            <signUp ref="signUpRef" @handle="signUpHandle"></signUp>
        </el-dialog>
    </div>
</template>
<script lang="ts">
export default {
    auto: true,
    route: { name: "login", path: "/login", meta: { guest: true } },
};
</script>
<script setup lang="ts">
import user from "@/store/user";
import signUp from "@/views/pages/systemManagement/user/signUp.vue";
// 基础项目名称
const BASE_PROJECT_NAME = computed(() => {
    return process.env.BASE_PROJECT_NAME as string
})
const BASE_PROJECT_LOGO_URL_BIG = computed(() => {
    return process.env.BASE_PROJECT_LOGO_URL_BIG as string;
});
const userData = ref<any>({
    username: "",
    password: "",
});

const userStore = user();
const userLogin = () => {
    userStore.userLogin(userData.value);
};
let signUpVisible = ref(false);
const signUpRef = ref<any>();
const openSignUp = () => {
    signUpVisible.value = true;
    nextTick(() => {
        signUpRef.value!.handleFun('add', {})
    })
};
const signUpHandle = (t: string, d: any = null) => {
    if (t === 'saveEnd') {
        signUpVisible.value = false
    }
    if (t === 'close') {
        signUpVisible.value = false
    }
}
</script>

<style lang="scss" scoped></style>
