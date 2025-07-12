package com.langhuan.utils.pagination;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * JdbcTemplate分页查询工具类
 * 实现与MyBatis Plus分页插件相同的数据输出格式
 * 专门针对PostgreSQL数据库优化
 *
 * @author system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JdbcPaginationHelper {

    private final JdbcTemplate jdbcTemplate;

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
    public <T> IPage<T> selectPage(String dataSql, String countSql, Object[] params,
            RowMapper<T> rowMapper, long current, long size) {
        // 参数验证
        if (current < 1) {
            current = 1;
        }
        if (size < 1) {
            size = 10;
        }

        // 创建分页对象
        JdbcPage<T> page = new JdbcPage<>(current, size);

        try {
            // 执行计数查询
            long total = executeCountQuery(dataSql, countSql, params);
            page.setTotal(total);
            

            // 如果总记录数为0，直接返回空结果
            if (total == 0) {
                return page;
            }

            // 计算偏移量
            long offset = (current - 1) * size;

            // 构建分页查询SQL
            String paginationSql = buildPaginationSql(dataSql, offset, size);

            // 记录数据查询SQL
            log.info("执行数据查询SQL: {}", paginationSql);

            // 执行数据查询
            List<T> records = jdbcTemplate.query(paginationSql, rowMapper, params);
            page.setRecords(records);
            

        } catch (Exception e) {
            log.error("分页查询执行失败: dataSql={}, countSql={}, params={}, current={}, size={}", 
                    dataSql, countSql, params, current, size, e);
            throw new RuntimeException("分页查询执行失败", e);
        }

        return page;
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
    public IPage<Map<String, Object>> selectPageForMap(String dataSql, String countSql,
            Object[] params, long current, long size) {
        
        return selectPage(dataSql, countSql, params, (rs, rowNum) -> {
            Map<String, Object> row = new java.util.HashMap<>();
            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rs.getMetaData().getColumnLabel(i);
                Object value = rs.getObject(i);
                // 转换为驼峰命名
                String camelCaseName = convertToCamelCase(columnName);
                row.put(camelCaseName, value);
            }
            return row;
        }, current, size);
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
    public <T> IPage<T> selectPage(String dataSql, Object[] params, RowMapper<T> rowMapper,
            long current, long size) {
        return selectPage(dataSql, null, params, rowMapper, current, size);
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
    public IPage<Map<String, Object>> selectPageForMap(String dataSql, Object[] params,
            long current, long size) {
        return selectPageForMap(dataSql, null, params, current, size);
    }

    /**
     * 执行计数查询
     *
     * @param dataSql  数据查询SQL
     * @param countSql 计数查询SQL
     * @param params   查询参数
     * @return 总记录数
     */
    private long executeCountQuery(String dataSql, String countSql, Object[] params) {
        String finalCountSql;

        if (StringUtils.hasText(countSql)) {
            finalCountSql = countSql;
        } else {
            // 自动生成计数SQL
            finalCountSql = generateCountSql(dataSql);
        }

        // 记录计数查询SQL
        log.info("执行计数查询SQL: {}", finalCountSql);

        try {
            Long result = jdbcTemplate.queryForObject(finalCountSql, Long.class, params);
            long count = result != null ? result : 0L;
            return count;
        } catch (Exception e) {
            log.error("计数查询执行失败: countSql={}, params={}", finalCountSql, params, e);
            throw new RuntimeException("计数查询执行失败", e);
        }
    }

    /**
     * 自动生成计数SQL
     * 针对PostgreSQL数据库优化
     *
     * @param dataSql 数据查询SQL
     * @return 计数SQL
     */
    private String generateCountSql(String dataSql) {
        // 移除末尾的分号
        String sql = dataSql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        // 转换为小写进行匹配（但保留原始SQL的大小写）
        String lowerSql = sql.toLowerCase();

        // 查找主要的SELECT部分
        int selectIndex = lowerSql.indexOf("select");
        if (selectIndex == -1) {
            log.error("无效的SQL语句，未找到SELECT关键字: {}", sql);
            throw new IllegalArgumentException("无效的SQL语句，未找到SELECT关键字");
        }

        // 查找FROM关键字
        int fromIndex = lowerSql.indexOf("from");
        if (fromIndex == -1) {
            log.error("无效的SQL语句，未找到FROM关键字: {}", sql);
            throw new IllegalArgumentException("无效的SQL语句，未找到FROM关键字");
        }

        // 提取FROM之后的部分
        String fromPart = sql.substring(fromIndex);

        // 检查是否有ORDER BY子句并移除
        String lowerFromPart = fromPart.toLowerCase();
        int orderByIndex = lowerFromPart.lastIndexOf("order by");
        if (orderByIndex != -1) {
            fromPart = fromPart.substring(0, orderByIndex).trim();
        }

        // 构建计数SQL
        String countSql = "SELECT COUNT(*) " + fromPart;
        return countSql;
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
    private String buildPaginationSql(String dataSql, long offset, long limit) {
        // 移除末尾的分号
        String sql = dataSql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        // 添加PostgreSQL分页语法
        String paginationSql = sql + " LIMIT " + limit + " OFFSET " + offset;
        return paginationSql;
    }

    /**
     * 将下划线命名转换为驼峰命名
     * 例如：user_name -> userName, created_at -> createdAt
     *
     * @param underscoreName 下划线命名的字符串
     * @return 驼峰命名的字符串
     */
    private String convertToCamelCase(String underscoreName) {
        if (underscoreName == null || underscoreName.isEmpty()) {
            return underscoreName;
        }

        // 使用Hutool的字符串工具进行转换
        String camelCase = cn.hutool.core.util.StrUtil.toCamelCase(underscoreName);
        return camelCase;
    }

    /**
     * 构建查询条件
     * 工具方法，用于动态构建WHERE条件
     *
     * @param conditions 条件Map，key为字段名，value为字段值
     * @param operator   操作符（AND/OR）
     * @return SQL条件字符串和参数数组
     */
    public static class QueryCondition {
        private final StringBuilder whereClause = new StringBuilder();
        private final java.util.List<Object> params = new java.util.ArrayList<>();

        /**
         * 添加等于条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        public QueryCondition eq(String field, Object value) {
            if (value != null) {
                addCondition(field + " = ?", value);
            }
            return this;
        }

        /**
         * 添加模糊查询条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        public QueryCondition like(String field, String value) {
            if (StringUtils.hasText(value)) {
                addCondition(field + " LIKE ?", "%" + value + "%");
            }
            return this;
        }

        /**
         * 添加大于等于条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        public QueryCondition ge(String field, Object value) {
            if (value != null) {
                addCondition(field + " >= ?", value);
            }
            return this;
        }

        /**
         * 添加小于等于条件
         *
         * @param field 字段名
         * @param value 字段值
         * @return 当前对象
         */
        public QueryCondition le(String field, Object value) {
            if (value != null) {
                addCondition(field + " <= ?", value);
            }
            return this;
        }

        /**
         * 添加IN条件
         *
         * @param field  字段名
         * @param values 值列表
         * @return 当前对象
         */
        public QueryCondition in(String field, List<?> values) {
            if (values != null && !values.isEmpty()) {
                String placeholders = String.join(",", java.util.Collections.nCopies(values.size(), "?"));
                addCondition(field + " IN (" + placeholders + ")", values.toArray());
            }
            return this;
        }

        /**
         * 添加自定义条件
         *
         * @param condition 条件SQL
         * @param params    参数
         * @return 当前对象
         */
        public QueryCondition custom(String condition, Object... params) {
            if (StringUtils.hasText(condition)) {
                addCondition(condition, params);
            }
            return this;
        }

        /**
         * 添加条件
         *
         * @param condition 条件SQL
         * @param params    参数
         */
        private void addCondition(String condition, Object... params) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            whereClause.append(condition);
            if (params != null) {
                for (Object param : params) {
                    this.params.add(param);
                }
            }
        }

        /**
         * 获取WHERE子句
         *
         * @return WHERE子句
         */
        public String getWhereClause() {
            return whereClause.length() > 0 ? " WHERE " + whereClause.toString() : "";
        }

        /**
         * 获取参数数组
         *
         * @return 参数数组
         */
        public Object[] getParams() {
            return params.toArray();
        }

        /**
         * 是否有条件
         *
         * @return 是否有条件
         */
        public boolean hasConditions() {
            return whereClause.length() > 0;
        }
    }
}