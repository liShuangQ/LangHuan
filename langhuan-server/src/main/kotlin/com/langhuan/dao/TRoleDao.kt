package com.langhuan.dao

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * 角色数据访问层
 * 处理角色相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
class TRoleDao(
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val log = LoggerFactory.getLogger(TRoleDao::class.java)
    }

    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID，如果为null则查询所有权限
     * @return 权限列表
     */
    fun getRolePermission(roleId: Int?): List<Map<String, Any>> {
        val sql = StringBuilder()
        return if (roleId != null) {
            sql.append(
                """
                select
                    p.id as permission_id,
                    p.name as permission_name
                from t_role_permission rp
                         left join t_role r on rp.role_id = r.id
                         left join t_permission p on rp.permission_id = p.id
                where 1 = 1
                and r.id = ?
                """.trimIndent()
            )
            jdbcTemplate.queryForList(sql.toString(), roleId)
        } else {
            sql.append(
                """
                select  p.id as permission_id,
                        p.name as permission_name
                from t_permission p
                """.trimIndent()
            )
            jdbcTemplate.queryForList(sql.toString())
        }
    }
}
