import Axios from "./Axios";

// axios基础设置
const http: Axios = new Axios({
    baseURL: process.env.BASE_URL,
    timeout: 300000,
    q_throttle: false, // 自定义 是否开启节流
    q_spinning: true,  // 自定义 是否出现旋转蒙层
    headers: {},
});

export {http};

