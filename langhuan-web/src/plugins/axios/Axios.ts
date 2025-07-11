import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { ElLoading, ElMessage } from "element-plus";
import qs from "qs";
import { store } from "@/utils";

export interface MyAxiosRequestConfig extends AxiosRequestConfig {
    /**
     * 是否节流
     */
    q_throttle?: boolean;
    /**
     * 是否蒙层
     */
    q_spinning?: boolean;
    /**
     * 影响请求头中的 Content-Type
     * form: application/x-www-form-urlencoded
     * json: application/json
     */
    q_contentType?: "form" | "json" | "formfile";
    q_baseUrl?: string;
    q_headers?: any;
}

export default class Axios {
    private instance: AxiosInstance;
    private lastTime: number;
    private throttleTime: number;
    private loadingInstance: any;
    private isHandling401: boolean; // 添加401处理标志位

    constructor(config: MyAxiosRequestConfig) {
        this.instance = axios.create(config);
        this.interceptors();
        this.lastTime = 0;
        this.throttleTime = 2000;
        this.loadingInstance = null;
        this.isHandling401 = false; // 初始化401处理标志位
    }

    public request<T, D = ResponseResult<T>, R = any>(
        config: MyAxiosRequestConfig
    ): Promise<D> {
        return new Promise(async (res, rej): Promise<void> => {
            try {
                config["method"] = config?.method ?? "post";
                config["q_contentType"] = config?.q_contentType ?? "form";
                if (config.q_contentType === "form") {
                    config.data = qs.stringify(config.data);
                    // config.headers && (config.headers['Content-Type'] = 'application/x-www-form-urlencoded')
                    config.headers &&
                        (config.headers["Content-Type"] =
                            "multipart/form-data ");
                } else if (config.q_contentType === "formfile") {
                    config.headers &&
                        (config.headers["Content-Type"] =
                            "multipart/form-data ");
                } else {
                    config.headers &&
                        (config.headers["Content-Type"] = "application/json");
                }

                const response: AxiosResponse<D, any> =
                    await this.instance.request<D>(config);
                //处理直接返回数据
                res(response.data);
            } catch (error) {
                rej(error);
            }
        }) as Promise<D>;
    }

    public jsonp(url: string, data: any): Promise<any> {
        if (!url) throw new Error("url is necessary");
        const callback: string =
            "CALLBACK" + Math.random().toString().substr(9, 18);
        const JSONP: HTMLScriptElement = document.createElement("script");
        JSONP.setAttribute("type", "text/javascript");
        const headEle: HTMLHeadElement =
            document.getElementsByTagName("head")[0];
        let ret: string = "";
        if (data) {
            if (typeof data === "string") ret = "&" + data;
            else if (typeof data === "object") {
                for (let key in data)
                    ret += "&" + key + "=" + encodeURIComponent(data[key]);
            }
            ret += "&_time=" + Date.now();
        }
        JSONP.src = `${url}?callback=${callback}${ret}`;
        return new Promise((resolve): void => {
            (window as any)[callback] = (r: any): void => {
                resolve(r);
                headEle.removeChild(JSONP);
                delete (window as any)[callback];
            };
            headEle.appendChild(JSONP);
        });
    }

    private interceptors(): void {
        // 添加请求拦截器
        this.interceptorsRequest();
        // 添加响应拦截器
        this.interceptorsResponse();
    }

    private interceptorsRequest(): void {
        this.instance.interceptors.request.use(
            (config: MyAxiosRequestConfig): any => {
                if (config.q_throttle) {
                    const nowTime: number = new Date().getTime();

                    if (nowTime - this.lastTime < this.throttleTime) {
                        return Promise.reject({
                            response: { status: "Throttling" },
                        });
                    }
                    this.lastTime = nowTime;
                }
                config.q_spinning &&
                    (this.loadingInstance = ElLoading.service({
                        lock: true,
                        text: "Loading",
                        background: "rgba(0, 0, 0, 0.7)",
                    }));
                config.q_baseUrl && (config.baseURL = config.q_baseUrl);

                config.headers &&
                    (config.headers[process.env.TOKEN_KEY as string] =
                        store.token());
                config.headers = {
                    ...config.headers,
                    ...config.q_headers,
                };

                return config;
            },
            (error) => {
                // 对请求错误做些什么
                return Promise.reject(error);
            }
        );
    }

    private interceptorsResponse(): void {
        this.instance.interceptors.response.use(
            // (response: AxiosResponse<any, any>) => {
            (response: any) => {
                this.loadingInstance && this.loadingInstance.close();

                // if ([500, 555, 777].includes(response.data.code)) {
                //     ElMessage.error(response.data?.message ?? "请求失败。");
                // }
                if (
                    response.data.code &&
                    ![200].includes(response.data.code) &&
                    !response.config.q_baseUrl
                ) {
                    ElMessage.error(response.data?.message ?? "请求失败。");
                }
                // 2xx 范围内的状态码都会触发该函数。
                return response;
            },
            (error) => {
                this.loadingInstance && this.loadingInstance.close();
                console.log(error);

                // 超出 2xx 范围的状态码都会触发该函数。
                switch (error.response.status) {
                    // case 'Throttling':
                    //     break;
                    case 401:
                        if (!this.isHandling401) {
                            ElMessage.warning("登录已过期，请重新登录。");
                            this.isHandling401 = true;
                            setTimeout(() => {
                                localStorage.removeItem(
                                    process.env.TOKEN_KEY as string
                                );
                                window.location.href = "/";
                            }, 1500);
                        }
                        break;
                    // case 422:
                    //     break;
                    default:
                        ElMessage.error("请求失败，请联系管理员。");
                }
                return Promise.reject(error);
            }
        );
    }
}
