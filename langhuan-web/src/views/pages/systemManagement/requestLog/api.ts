import { http } from "@/plugins/axios";

// 查询接口日志列表
export function searchApiLogs(params: {
    current?: number;
    size?: number;
    apiName?: string;
    username?: string;
    httpMethod?: string;
    isSuccess?: boolean;
    startTime?: string;
    endTime?: string;
}) {
    return http.request<any>({
        url: '/api/log/search',
        method: 'post',
        q_spinning: true,
        q_contentType: 'form',
        data: params
    });
}

// 获取接口调用统计信息
export function getApiLogStatistics() {
    return http.request<any>({
        url: '/api/log/statistics',
        method: 'post',
        q_spinning: true,
        q_contentType: 'form'
    });
}

// 删除接口日志
export function deleteApiLog(id: number) {
    return http.request<any>({
        url: '/api/log/delete',
        method: 'post',
        q_spinning: true,
        q_contentType: 'form',
        data: { id }
    });
}

// 批量删除接口日志
export function batchDeleteApiLogs(ids: number[]) {
    return http.request<any>({
        url: '/api/log/batchDelete',
        method: 'post',
        q_spinning: true,
        q_contentType: 'json',
        data: ids
    });
}

// 清理历史日志
export function cleanHistoryLogs(days: number = 30) {
    return http.request<any>({
        url: '/api/log/clean',
        method: 'post',
        q_spinning: true,
        q_contentType: 'form',
        data: { days }
    });
}