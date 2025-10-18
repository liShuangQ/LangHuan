package com.langhuan.dao

import com.baomidou.mybatisplus.core.metadata.IPage
import com.langhuan.utils.pagination.JdbcPaginationHelper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.stream.Collectors

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group】的数据库操作Dao实现
 * @createDate 2025-01-17 15:08:49
 */
@Repository
class TRagFileGroupDao(
    private val paginationHelper: JdbcPaginationHelper,
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(TRagFileGroupDao::class.java)
    }

    /**
     * 管理员获取所有文件组枚举列表
     * 
     * @return 文件组枚举列表
     */
    fun getEnumForAdmin(): List<Map<String, Any>> {
        log.info("getEnumForAdmin")
        val sql = """
            SELECT
                fg.id,
                fg.group_name as "groupName"
            FROM t_rag_file_group fg
            LEFT JOIN t_user u ON fg.created_by = u.username
            WHERE 1=1
            """.trimIndent()

        val queryForList = jdbcTemplate.queryForList(sql)

        return queryForList.stream()
            .map { e -> mapOf("id" to e["id"]!!, "groupName" to e["groupName"]!!) }
            .collect(Collectors.toList())
    }

    /**
     * 普通用户根据权限获取文件组枚举列表
     * 
     * @param isRead      是否为读取权限
     * @param currentUser 当前用户
     * @return 文件组枚举列表
     */
    fun getEnumForUser(isRead: Boolean, currentUser: String): List<Map<String, Any>> {
        log.info("getEnumForUser: isRead={}, currentUser={}", isRead, currentUser)
        val dataSql = StringBuilder()
        val dataParams = ArrayList<Any>()

        // 普通用户查询文件组（公开的 + 自己创建的 + 被分享的）
        dataSql.append(
            """
                SELECT DISTINCT
                    fg.id,
                    fg.group_name as "groupName"
                FROM t_rag_file_group fg
                LEFT JOIN t_user u ON fg.created_by = u.username
                LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ?
                """.trimIndent())
        if (isRead) {
            dataSql.append("AND fgs.can_read = TRUE")
        }
        if (!isRead) {
            dataSql.append("AND fgs.can_update = TRUE OR fgs.can_add = TRUE")
        }

        dataSql.append(
            """
                WHERE
                fg.visibility = 'public'  -- 公开的文件组
                OR fg.created_by = ?      -- 自己创建的文件组
                OR fgs.id IS NOT NULL     -- 被分享的文件组
            """.trimIndent())

        // 添加参数（按顺序）
        dataParams.add(currentUser)
        dataParams.add(currentUser)

        val queryForList = jdbcTemplate.queryForList(dataSql.toString(), dataParams.toArray())

        return queryForList.stream()
            .map { e -> mapOf("id" to e["id"]!!, "groupName" to e["groupName"]!!) }
            .collect(Collectors.toList())
    }

    /**
     * 管理员查询所有文件组分页数据
     * 
     * @param groupName  文件组名称
     * @param groupType  文件组类型
     * @param visibility 可见性
     * @param pageNum    页码
     * @param pageSize   页大小
     * @return 分页查询结果
     */
    fun queryFileGroupsForAdmin(
        groupName: String?,
        groupType: String?,
        visibility: String?,
        pageNum: Int,
        pageSize: Int
    ): IPage<Map<String, Any>> {
        log.info("queryFileGroupsForAdmin: groupName={}, groupType={}, visibility={}, pageNum={}, pageSize={}",
            groupName, groupType, visibility, pageNum, pageSize)
        val dataSql = StringBuilder()
        val countSql = StringBuilder()
        val dataParams = ArrayList<Any>()
        val countParams = ArrayList<Any>()

        // 超级管理员查询所有文件组
        dataSql.append("""
            SELECT
                fg.id,
                fg.group_name as "groupName",
                fg.group_type as "groupType",
                fg.group_desc as "groupDesc",
                fg.visibility,
                fg.created_by as "createdBy",
                fg.created_at as "createdAt",
                u.name as "userName"
            FROM t_rag_file_group fg
            LEFT JOIN t_user u ON fg.created_by = u.username
            WHERE 1=1
            """.trimIndent())

        countSql.append("""
            SELECT COUNT(*)
            FROM t_rag_file_group fg
            WHERE 1=1
            """.trimIndent())

        // 构建WHERE条件
        if (!groupName.isNullOrBlank()) {
            dataSql.append(" AND fg.group_name LIKE ?")
            countSql.append(" AND fg.group_name LIKE ?")
            dataParams.add("%$groupName%")
            countParams.add("%$groupName%")
        }

        if (!groupType.isNullOrBlank()) {
            dataSql.append(" AND fg.group_type LIKE ?")
            countSql.append(" AND fg.group_type LIKE ?")
            dataParams.add("%$groupType%")
            countParams.add("%$groupType%")
        }

        if (!visibility.isNullOrBlank()) {
            dataSql.append(" AND fg.visibility = ?")
            countSql.append(" AND fg.visibility = ?")
            dataParams.add(visibility)
            countParams.add(visibility)
        }

        dataSql.append(" ORDER BY fg.created_at DESC")

        // 执行分页查询
        return paginationHelper.selectPageForMapWithDifferentParams(
            dataSql.toString(), countSql.toString(),
            dataParams.toTypedArray(), countParams.toTypedArray(),
            pageNum.toLong(), pageSize.toLong())
    }

    /**
     * 普通用户查询文件组分页数据（公开的 + 自己创建的 + 被分享的）
     * 
     * @param groupName   文件组名称
     * @param groupType   文件组类型
     * @param visibility  可见性
     * @param pageNum     页码
     * @param pageSize    页大小
     * @param currentUser 当前用户
     * @return 分页查询结果
     */
    fun queryFileGroupsForUser(
        groupName: String?,
        groupType: String?,
        visibility: String?,
        pageNum: Int,
        pageSize: Int,
        currentUser: String
    ): IPage<Map<String, Any>> {
        log.info(
            "queryFileGroupsForUser: groupName={}, groupType={}, visibility={}, pageNum={}, pageSize={}, currentUser={}",
            groupName, groupType, visibility, pageNum, pageSize, currentUser)
        val dataSql = StringBuilder()
        val countSql = StringBuilder()
        val dataParams = ArrayList<Any>()
        val countParams = ArrayList<Any>()

        // 普通用户查询文件组（公开的 + 自己创建的 + 被分享的）
        dataSql.append(
            """
                SELECT DISTINCT
                    fg.id,
                    fg.group_name as "groupName",
                    fg.group_type as "groupType",
                    fg.group_desc as "groupDesc",
                    fg.visibility,
                    fg.created_by as "createdBy",
                    fg.created_at as "createdAt",
                    u.name as "userName"
                FROM t_rag_file_group fg
                LEFT JOIN t_user u ON fg.created_by = u.username
                LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ? AND fgs.can_read = TRUE
                WHERE
                    fg.visibility = 'public'  -- 公开的文件组
                    OR fg.created_by = ?      -- 自己创建的文件组
                    OR fgs.id IS NOT NULL     -- 被分享的文件组
                """.trimIndent())

        countSql.append(
            """
                SELECT COUNT(DISTINCT fg.id)
                FROM t_rag_file_group fg
                LEFT JOIN t_rag_file_group_share fgs ON fg.id = fgs.file_group_id AND fgs.shared_with = ? AND fgs.can_read = TRUE
                WHERE
                    fg.visibility = 'public'  -- 公开的文件组
                    OR fg.created_by = ?      -- 自己创建的文件组
                    OR fgs.id IS NOT NULL     -- 被分享的文件组
                """.trimIndent())

        // 添加基础参数（按顺序）
        dataParams.add(currentUser) // LEFT JOIN中的shared_with
        dataParams.add(currentUser) // WHERE中的created_by

        // count查询的参数
        countParams.add(currentUser) // LEFT JOIN中的shared_with
        countParams.add(currentUser) // WHERE中的created_by

        // 构建额外的WHERE条件
        if (!groupName.isNullOrBlank()) {
            dataSql.append(" AND fg.group_name LIKE ?")
            countSql.append(" AND fg.group_name LIKE ?")
            dataParams.add("%$groupName%")
            countParams.add("%$groupName%")
        }

        if (!groupType.isNullOrBlank()) {
            dataSql.append(" AND fg.group_type LIKE ?")
            countSql.append(" AND fg.group_type LIKE ?")
            dataParams.add("%$groupType%")
            countParams.add("%$groupType%")
        }

        if (!visibility.isNullOrBlank()) {
            dataSql.append(" AND fg.visibility = ?")
            countSql.append(" AND fg.visibility = ?")
            dataParams.add(visibility)
            countParams.add(visibility)
        }

        dataSql.append(" ORDER BY fg.created_at DESC")

        // 执行分页查询
        return paginationHelper.selectPageForMapWithDifferentParams(
            dataSql.toString(), countSql.toString(),
            dataParams.toTypedArray(), countParams.toTypedArray(),
            pageNum.toLong(), pageSize.toLong())
    }
}
