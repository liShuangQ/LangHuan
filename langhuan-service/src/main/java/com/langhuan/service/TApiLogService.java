package com.langhuan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TApiLog;
import com.langhuan.model.mapper.TApiLogMapper;
import com.langhuan.utils.pagination.JdbcPaginationHelper;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 接口调用日志统计服务实现类
 *
 * @author system
 */
@Slf4j
@Service
public class TApiLogService extends ServiceImpl<TApiLogMapper, TApiLog> {

    private final JdbcPaginationHelper paginationHelper;

    public TApiLogService(JdbcPaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    public IPage<Map<String, Object>> search(String apiName, String username, LocalDateTime startTime,
            LocalDateTime endTime, int pageNum, int pageSize) {

        JdbcPaginationHelper.QueryCondition condition = new JdbcPaginationHelper.QueryCondition();
        if (apiName != null && !apiName.isEmpty()) {
            condition.like("api_name", apiName);
        }
        if (username != null && !username.isEmpty()) {
            condition.like("u.name", username);
        }
        if (startTime != null) {
            condition.ge("al.create_time", startTime);
        }
        if (endTime != null) {
            condition.le("al.create_time", endTime);
        }
        String sql = "SELECT al.*, u.name as user_name FROM t_api_log al LEFT JOIN t_user u ON al.user_id = u.username"
                + condition.getWhereClause() + " ORDER BY al.create_time DESC";
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum, pageSize);
    }

    public boolean saveApiLog(TApiLog apiLog) {
        try {
            return save(apiLog);
        } catch (Exception e) {
            log.error("保存接口调用日志失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Async
    public void saveApiLogAsync(TApiLog apiLog) {
        try {
            save(apiLog);
            log.debug("异步保存接口调用日志成功: {}", apiLog.getApiName());
        } catch (Exception e) {
            log.error("异步保存接口调用日志失败: {}", e.getMessage(), e);
        }
    }
}