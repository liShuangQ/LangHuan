import { http } from "@/plugins/axios";
// 文档的点踩机制
export const documentRankHandleApi = (
    id: string,
    nowRank: number,
    type: "good" | "bad"
) => {
    return http.request<any>({
        url: "/rag/changeDocumentsRank",
        method: "post",
        q_spinning: true,
        data: {
            id: id,
            rank: type === "good" ? nowRank + 1 : nowRank - 1,
        },
    });
};
