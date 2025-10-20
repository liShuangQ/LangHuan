import { defineStore } from "pinia";
import { http } from "@/plugins/axios";

export default defineStore("aimodel", {
    state: (): {
        modelOptions: any | { label: string; value: string }[];
    } => {
        return {
            modelOptions: null,
        };
    },
    actions: {
        async setModelOptions() {
            await http
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
                                return (
                                    e.id.indexOf("embed") === -1 &&
                                    e.id.indexOf("Embed") === -1
                                );
                            })
                            .filter((e: any) => {
                                return e.id.indexOf("rerank") === -1;
                            })
                            .map((e: any) => {
                                return {
                                    label: e.id,
                                    value: e.id,
                                };
                            });
                    }
                });
        },
        async getModelOptions() {
            if (this.modelOptions === null) {
                await this.setModelOptions();
            }
            return this.modelOptions;
        },
    },
});
