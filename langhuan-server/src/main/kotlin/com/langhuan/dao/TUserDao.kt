package com.langhuan.dao

import com.langhuan.utils.other.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * 用户数据访问层
 * 处理用户相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
class TUserDao(
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(TUserDao::class.java)
    }

    /**
     * 根据用户ID获取用户角色信息
     *
     * @param userId 用户ID
     * @return 用户角色列表
     */
    fun getUserRolesById(userId: Int?): List<Map<String, Any>> {
        val sql = StringBuilder()
        return if (userId != null) {
            sql.append("""
                select
                    r.id as role_id,
                    r.name as role_name
                from t_user_role ur
                         left join t_user u on ur.user_id = u.id
                         left join t_role r on ur.role_id = r.id
                where 1 = 1
                and u.id = ?
                """.trimIndent())
            jdbcTemplate.queryForList(sql.toString(), userId)
        } else {
            sql.append("""
                select r.id as role_id, r.name as role_name
                from t_role r;
                """.trimIndent())
            jdbcTemplate.queryForList(sql.toString())
        }
    }

    /**
     * 根据用户名获取用户角色信息
     *
     * @param userName 用户名
     * @return 用户角色列表
     */
    fun getUserRolesByName(userName: String?): List<Map<String, Any>> {
        val sql = StringBuilder()
        return if (userName != null) {
            sql.append("""
                select
                    r.id as role_id,
                    r.name as role_name
                from t_user_role ur
                         left join t_user u on ur.user_id = u.id
                         left join t_role r on ur.role_id = r.id
                where 1 = 1
                and u.username = ?
                """.trimIndent())
            jdbcTemplate.queryForList(sql.toString(), userName)
        } else {
            sql.append("""
                select r.id as role_id, r.name as role_name
                from t_role r;
                """.trimIndent())
            jdbcTemplate.queryForList(sql.toString())
        }
    }

    /**
     * 根据用户ID获取用户详细信息（包括角色信息）
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    fun getUserInfoById(userId: Int): List<Map<String, Any>> {
        return jdbcTemplate.queryForList("""
            select r.* from t_user u
            left join t_user_role ur on u.id = ur.user_id
            left join t_role r on ur.role_id = r.id
            where u.id = ?
            """.trimIndent(), userId)
    }

    fun getUserRoleListById(roleId: Int?): List<Map<String, Any>> {
        val currentUser = SecurityUtils.getCurrentUsername() ?: ""
        var sql = """
            SELECT
                DISTINCT
                u.id AS user_id,
                u.username AS username,
                u.name AS name,
                r.id AS roleId,
                CASE WHEN r.name IS NOT NULL THEN r.name ELSE '访客' END AS roleName
            FROM t_user u
            LEFT JOIN t_user_role ur ON ur.user_id = u.id
            LEFT JOIN t_role r ON r.id = ur.role_id
            WHERE u.username != ?
            """.trimIndent()
        
        return if (roleId != null) {
            sql += " AND r.id = ?"
            jdbcTemplate.queryForList(sql, currentUser, roleId)
        } else {
            jdbcTemplate.queryForList(sql, currentUser)
        }
    }
}
