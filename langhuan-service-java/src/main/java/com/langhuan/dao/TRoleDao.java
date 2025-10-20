package com.langhuan.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 角色数据访问层
 * 处理角色相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
@Slf4j
public class TRoleDao {

    private final JdbcTemplate jdbcTemplate;

    public TRoleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID，如果为null则查询所有权限
     * @return 权限列表
     */
    public List<Map<String, Object>> getRolePermission(Integer roleId) {
        StringBuilder sql = new StringBuilder();
        if (roleId != null) {
            sql.append("""
                    select
                        p.id as permission_id,
                        p.name as permission_name
                    from t_role_permission rp
                             left join t_role r on rp.role_id = r.id
                             left join t_permission p on rp.permission_id = p.id
                    where 1 = 1
                    and r.id = ?
                    """);
            return jdbcTemplate.queryForList(sql.toString(), List.of(roleId).toArray());
        } else {
            sql.append("""
                        select  p.id as permission_id,
                                p.name as permission_name
                        from t_permission p
                    """);
            return jdbcTemplate.queryForList(sql.toString());
        }
    }
}