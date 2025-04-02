import { defineStore } from "pinia";
import { http } from "@/plugins/axios";

export default defineStore("aimodel", {
    state: (): { modelOptions: object | null } => {
        return {
            modelOptions: null,
        };
    },
    actions: {
        async setModelOptions() {
            const oneapi = process.env.IS_ONEAPI_GET_MODEL as string;
            oneapi === "false" &&
                (await http
                    .request<any>({
                        url: "/chatModel/getModelList",
                        method: "post",
                        q_spinning: true,
                        data: {},
                    })
                    .then((res) => {
                        if (res.code === 200) {
                            this.modelOptions = res.data.data
                                .filter((e: any) => {
                                    return e.id.indexOf("embed") === -1;
                                })
                                .map((e: any) => {
                                    return {
                                        label: e.id,
                                        value: e.id,
                                    };
                                });
                        }
                    }));

            oneapi === "true" &&
                (await http
                    .request<any>({
                        url: "/v1/models",
                        method: "get",
                        q_baseUrl: process.env.ONEAPI_IP,
                        q_spinning: true,
                        q_headers: {
                            Authorization: process.env.ONEAPI_TOKEN,
                        },
                        data: {},
                    })
                    .then((res) => {
                        this.modelOptions = res.data
                            .filter((e: any) => {
                                return e.id.indexOf("embed") === -1;
                            })
                            .map((e: any) => {
                                return {
                                    label: e.id,
                                    value: e.id,
                                };
                            });
                    }));
        },
        getModelOptions() {
            return this.modelOptions;
        },
    },
});
