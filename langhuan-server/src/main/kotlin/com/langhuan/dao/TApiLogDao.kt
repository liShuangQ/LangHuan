package com.langhuan.dao

import com.baomidou.mybatisplus.core.metadata.IPage
import com.langhuan.utils.pagination.JdbcPaginationHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 接口调用日志数据访问对象
 * 处理非MyBatis-Plus的原生SQL查询操作
 *
 * @author system
 */
@Repository
class TApiLogDao(
    private val paginationHelper: JdbcPaginationHelper
) {

    companion object {
        private val log = LoggerFactory.getLogger(TApiLogDao::class.java)
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
    fun searchApiLogs(
        apiName: String?,
        username: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        pageNum: Int,
        pageSize: Int
    ): IPage<java.util.Map<String, Any>> {
        
        val condition = JdbcPaginationHelper.QueryCondition()
        
        // 构建查询条件
        if (!apiName.isNullOrEmpty()) {
            condition.like("api_name", apiName)
        }
        if (!username.isNullOrEmpty()) {
            condition.like("u.name", username)
        }
        if (startTime != null) {
            condition.ge("al.create_time", startTime)
        }
        if (endTime != null) {
            condition.le("al.create_time", endTime)
        }
        
        // 构建SQL查询语句
        val sql = """
            SELECT al.*, u.name as user_name FROM t_api_log al LEFT JOIN t_user u ON al.user_id = u.username
            ${condition.getWhereClause()} ORDER BY al.create_time DESC
        """.trimIndent()
        
        log.debug("执行接口日志查询SQL: {}", sql)
        
        @Suppress("UNCHECKED_CAST")
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum.toLong(), pageSize.toLong()) as IPage<java.util.Map<String, Any>>
    }
}
