package com.langhuan.utils.pagination

import com.baomidou.mybatisplus.core.metadata.IPage
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.util.StringUtils

/**
 * JdbcTemplate分页查询工具类
 * 实现与MyBatis Plus分页插件相同的数据输出格式
 * 专门针对PostgreSQL数据库优化
 *
 * @author system
 */
class JdbcPaginationHelper(private val jdbcTemplate: JdbcTemplate) {

    companion object {
        private val log = LoggerFactory.getLogger(JdbcPaginationHelper::class.java)
    }

    /**
     * 执行分页查询
     *
     * @param dataSql   数据查询SQL
     * @param countSql  计数查询SQL（可选，如果为null则自动生成）
     * @param params    查询参数
     * @param rowMapper 行映射器
     * @param current   当前页码（从1开始）
     * @param size      每页大小
     * @param <T>       数据类型
     * @return 分页结果
     */
    fun <T> selectPage(
            dataSql: String,
            countSql: String?,
            params: Array<Any>?,
            rowMapper: RowMapper<T>,
            current: Long,
            size: Long
    ): IPage<T> {
        // 参数验证
        var current = current
        var size = size
        if (current < 1) {
            current = 1
        }
        if (size < 1) {
            size = 10
        }

        // 创建分页对象
        val page = JdbcPage<T>(current, size)

        try {
            // 执行计数查询
            val total = executeCountQuery(dataSql, countSql, params)
            page.total = total

            // 如果总记录数为0，直接返回空结果
            if (total == 0L) {
                return page
            }

            // 计算偏移量
            val offset = (current - 1) * size

            // 构建分页查询SQL
            val paginationSql = buildPaginationSql(dataSql, offset, size)

            // 记录数据查询SQL
            log.info("执行数据查询SQL: {}", paginationSql)

            // 执行数据查询
            val records = jdbcTemplate.query(paginationSql, rowMapper, *params ?: emptyArray())
            page.records = records

        } catch (e: Exception) {
            log.error("分页查询执行失败: dataSql={}, countSql={}, params={}, current={}, size={}",
                    dataSql, countSql, params, current, size, e)
            throw RuntimeException("分页查询执行失败", e)
        }

        return page
    }

    /**
     * 执行分页查询（使用Map作为返回类型）
     *
     * @param dataSql  数据查询SQL
     * @param countSql 计数查询SQL（可选）
     * @param params   查询参数
     * @param current  当前页码
     * @param size     每页大小
     * @return 分页结果
     */
    fun selectPageForMap(
            dataSql: String,
            countSql: String?,
            params: Array<Any>?,
            current: Long,
            size: Long
    ): IPage<Map<String, Any>> {
        
        return selectPage(dataSql, countSql, params, RowMapper { rs, rowNum ->
            val row = mutableMapOf<String, Any>()
            val columnCount = rs.metaData.columnCount
            for (i in 1..columnCount) {
                val columnName = rs.metaData.getColumnLabel(i)
                val value = rs.getObject(i)
                // 转换为驼峰命名
                val camelCaseName = convertToCamelCase(columnName)
                if (value != null) {
                    row[camelCaseName] = value
                }
            }
            row
        }, current, size)
    }

    /**
     * 执行分页查询（简化版本，自动生成计数SQL）
     *
     * @param dataSql   数据查询SQL
     * @param params    查询参数
     * @param rowMapper 行映射器
     * @param current   当前页码
     * @param size      每页大小
     * @param <T>       数据类型
     * @return 分页结果
     */
    fun <T> selectPage(
            dataSql: String,
            params: Array<Any>?,
            rowMapper: RowMapper<T>,
            current: Long,
            size: Long
    ): IPage<T> {
        return selectPage(dataSql, null, params, rowMapper, current, size)
    }

    /**
     * 执行分页查询（简化版本，使用Map返回）
     *
     * @param dataSql 数据查询SQL
     * @param params  查询参数
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页结果
     */
    fun selectPageForMap(
            dataSql: String,
            params: Array<Any>?,
            current: Long,
            size: Long
    ): IPage<Map<String, Any>> {
        return selectPageForMap(dataSql, null, params, current, size)
    }

    /**
     * 执行分页查询（支持不同的计数参数和数据参数）
     * 适用于复杂查询，计数查询和数据查询可能需要不同的参数
     *
     * @param dataSql     数据查询SQL
     * @param countSql    计数查询SQL
     * @param dataParams  数据查询参数
     * @param countParams 计数查询参数
     * @param current     当前页码
     * @param size        每页大小
     * @return 分页结果
     */
    fun selectPageForMapWithDifferentParams(
            dataSql: String,
            countSql: String?,
            dataParams: Array<Any>?,
            countParams: Array<Any>?,
            current: Long,
            size: Long
    ): IPage<Map<String, Any>> {
        
        // 参数验证
        var current = current
        var size = size
        if (current < 1) {
            current = 1
        }
        if (size < 1) {
            size = 10
        }

        // 创建分页对象
        val page = JdbcPage<Map<String, Any>>(current, size)

        try {
            // 执行计数查询
            val finalCountSql = if (StringUtils.hasText(countSql)) countSql else generateCountSql(dataSql)
            val finalCountParams = countParams ?: dataParams
            
            log.info("执行计数查询SQL: {}", finalCountSql)
            
            val result = jdbcTemplate.queryForObject(finalCountSql as  String, Long::class.java, *finalCountParams ?: emptyArray())
            val total = result ?: 0L
            page.total = total

            // 如果总记录数为0，直接返回空结果
            if (total == 0L) {
                return page
            }

            // 计算偏移量
            val offset = (current - 1) * size

            // 构建分页查询SQL
            val paginationSql = buildPaginationSql(dataSql, offset, size)

            // 记录数据查询SQL
            log.info("执行数据查询SQL: {}", paginationSql)

            // 执行数据查询
            val records = jdbcTemplate.query(paginationSql, { rs, rowNum ->
                val row = mutableMapOf<String, Any>()
                val columnCount = rs.metaData.columnCount
                for (i in 1..columnCount) {
                    val columnName = rs.metaData.getColumnLabel(i)
                    val value = rs.getObject(i)
                    // 转换为驼峰命名
                    val camelCaseName = convertToCamelCase(columnName)
                    if (value != null) {
                        row[camelCaseName] = value
                    }
                }
                row
            }, *dataParams ?: emptyArray())
            
            page.records = records

        } catch (e: Exception) {
            log.error("分页查询执行失败: dataSql={}, countSql={}, dataParams={}, countParams={}, current={}, size={}",
                    dataSql, countSql, dataParams, countParams, current, size, e)
            throw RuntimeException("分页查询执行失败", e)
        }

        return page
    }

    /**
     * 执行计数查询
     *
     * @param dataSql  数据查询SQL
     * @param countSql 计数查询SQL
     * @param params   查询参数
     * @return 总记录数
     */
    private fun executeCountQuery(dataSql: String, countSql: String?, params: Array<Any>?): Long {
        val finalCountSql: String

        if (StringUtils.hasText(countSql)) {
            finalCountSql = countSql as  String
        } else {
            // 自动生成计数SQL
            finalCountSql = generateCountSql(dataSql)
        }

        // 记录计数查询SQL
        log.info("执行计数查询SQL: {}", finalCountSql)

        return try {
            val result = jdbcTemplate.queryForObject(finalCountSql, Long::class.java, *params ?: emptyArray())
            result ?: 0L
        } catch (e: Exception) {
            log.error("计数查询执行失败: countSql={}, params={}", finalCountSql, params, e)
            throw RuntimeException("计数查询执行失败", e)
        }
    }

    /**
     * 自动生成计数SQL
     * 针对PostgreSQL数据库优化
     *
     * @param dataSql 数据查询SQL
     * @return 计数SQL
     */
    private fun generateCountSql(dataSql: String): String {
        // 移除末尾的分号
        var sql = dataSql.trim()
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length - 1)
        }

        // 转换为小写进行匹配（但保留原始SQL的大小写）
        val lowerSql = sql.lowercase()

        // 查找主要的SELECT部分
        val selectIndex = lowerSql.indexOf("select")
        if (selectIndex == -1) {
            log.error("无效的SQL语句，未找到SELECT关键字: {}", sql)
            throw IllegalArgumentException("无效的SQL语句，未找到SELECT关键字")
        }

        // 查找FROM关键字
        val fromIndex = lowerSql.indexOf("from")
        if (fromIndex == -1) {
            log.error("无效的SQL语句，未找到FROM关键字: {}", sql)
            throw IllegalArgumentException("无效的SQL语句，未找到FROM关键字")
        }

        // 提取FROM之后的部分
        var fromPart = sql.substring(fromIndex)

        // 检查是否有ORDER BY子句并移除
        val lowerFromPart = fromPart.lowercase()
        val orderByIndex = lowerFromPart.lastIndexOf("order by")
        if (orderByIndex != -1) {
            fromPart = fromPart.substring(0, orderByIndex).trim()
        }

        // 构建计数SQL
        return "SELECT COUNT(*) $fromPart"
    }

    /**
     * 构建分页查询SQL
     * 使用PostgreSQL的LIMIT和OFFSET语法
     *
     * @param dataSql 原始数据查询SQL
     * @param offset  偏移量
     * @param limit   限制数量
     * @return 分页SQL
     */
    private fun buildPaginationSql(dataSql: String, offset: Long, limit: Long): String {
        // 移除末尾的分号
        var sql = dataSql.trim()
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length - 1)
        }

        // 添加PostgreSQL分页语法
        return "$sql LIMIT $limit OFFSET $offset"
    }

    /**
     * 将下划线命名转换为驼峰命名
     * 例如：user_name -> userName, created_at -> createdAt
     *
     * @param underscoreName 下划线命名的字符串
     * @return 驼峰命名的字符串
     */
    private fun convertToCamelCase(underscoreName: String?): String {
        if (underscoreName.isNullOrEmpty()) {
            return underscoreName ?: ""
        }

        // 使用Hutool的字符串工具进行转换
        return cn.hutool.core.util.StrUtil.toCamelCase(underscoreName)
    }

    /**
     * 构建查询条件
     * 工具类，用于动态构建WHERE条件
     */
    class QueryCondition {
        private val whereClause = StringBuilder()
        private val params = mutableListOf<Any>()

        /**
         * 添加等于条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        fun eq(field: String, value: Any?): QueryCondition {
            if (value != null) {
                addCondition("$field = ?", value)
            }
            return this
        }

        /**
         * 添加模糊查询条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        fun like(field: String, value: String?): QueryCondition {
            if (StringUtils.hasText(value)) {
                addCondition("$field LIKE ?", "%$value%")
            }
            return this
        }

        /**
         * 添加大于等于条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        fun ge(field: String, value: Any?): QueryCondition {
            if (value != null) {
                addCondition("$field >= ?", value)
            }
            return this
        }

        /**
         * 添加小于等于条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        fun le(field: String, value: Any?): QueryCondition {
            if (value != null) {
                addCondition("$field <= ?", value)
            }
            return this
        }

        /**
         * 添加IN条件
         *
         * @param field  字段名
         * @param values 值列表
         * @return 当前对象
         */
        fun inClause(field: String, values: List<Any>?): QueryCondition {
            if (!values.isNullOrEmpty()) {
                val placeholders = values.joinToString(",") { "?" }
                addCondition("$field IN ($placeholders)", *values.toTypedArray())
            }
            return this
        }

        /**
         * 添加自定义条件
         *
         * @param condition 条件SQL
         * @param params    参数
         * @return 当前对象
         */
        fun custom(condition: String, vararg params: Any): QueryCondition {
            if (StringUtils.hasText(condition)) {
                addCondition(condition, *params)
            }
            return this
        }

        /**
         * 添加条件
         *
         * @param condition 条件SQL
         * @param params    参数
         */
        private fun addCondition(condition: String, vararg params: Any) {
            if (whereClause.isNotEmpty()) {
                whereClause.append(" AND ")
            }
            whereClause.append(condition)
            this.params.addAll(params)
        }

        /**
         * 获取WHERE子句
         *
         * @return WHERE子句
         */
        fun getWhereClause(): String {
            return if (whereClause.isNotEmpty()) " WHERE $whereClause" else ""
        }

        /**
         * 获取参数数组
         *
         * @return 参数数组
         */
        fun getParams(): Array<Any> {
            return params.toTypedArray()
        }

        /**
         * 是否有条件
         *
         * @return 是否有条件
         */
        fun hasConditions(): Boolean {
            return whereClause.isNotEmpty()
        }
    }
}
