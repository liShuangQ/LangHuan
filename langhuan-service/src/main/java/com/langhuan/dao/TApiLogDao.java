package com.langhuan.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.langhuan.utils.pagination.JdbcPaginationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 接口调用日志数据访问对象
 * 处理非MyBatis-Plus的原生SQL查询操作
 *
 * @author system
 */
@Repository
@Slf4j
public class TApiLogDao {

    private final JdbcPaginationHelper paginationHelper;

    public TApiLogDao(JdbcPaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

    /**
     * 分页查询接口调用日志
     * 支持按接口名称、用户名、时间范围进行筛选
     *
     * @param apiName   接口名称（模糊查询）
     * @param username  用户名（模糊查询）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页结果
     */
    public IPage<Map<String, Object>> searchApiLogs(String apiName, String username, LocalDateTime startTime,
                                                     LocalDateTime endTime, int pageNum, int pageSize) {
        
        JdbcPaginationHelper.QueryCondition condition = new JdbcPaginationHelper.QueryCondition();
        
        // 构建查询条件
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
        
        // 构建SQL查询语句
        String sql = "SELECT al.*, u.name as user_name FROM t_api_log al LEFT JOIN t_user u ON al.user_id = u.username"
                + condition.getWhereClause() + " ORDER BY al.create_time DESC";
        
        log.debug("执行接口日志查询SQL: {}", sql);
        
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum, pageSize);
    }
}