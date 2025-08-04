package com.langhuan.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 用户数据访问层
 * 处理用户相关的复杂SQL查询操作
 *
 * @author lishuangqi
 */
@Repository
@Slf4j
public class TUserDao {

    private final JdbcTemplate jdbcTemplate;

    public TUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据用户ID获取用户角色信息
     *
     * @param userId 用户ID
     * @return 用户角色列表
     */
    public List<Map<String, Object>> getUserRolesById(Integer userId) {
        StringBuilder sql = new StringBuilder();
        if (userId != null) {
            sql.append("""
                    select
                        r.id as role_id,
                        r.name as role_name
                    from t_user_role ur
                             left join t_user u on ur.user_id = u.id
                             left join t_role r on ur.role_id = r.id
                    where 1 = 1
                    and u.id = ?
                    """);
            return jdbcTemplate.queryForList(sql.toString(), List.of(userId).toArray());
        } else {
            sql.append("""
                        select r.id as role_id, r.name as role_name
                        from t_role r;
                    """);
            return jdbcTemplate.queryForList(sql.toString());
        }
    }

    /**
     * 根据用户名获取用户角色信息
     *
     * @param userName 用户名
     * @return 用户角色列表
     */
    public List<Map<String, Object>> getUserRolesByName(String userName) {
        StringBuilder sql = new StringBuilder();
        if (userName != null) {
            sql.append("""
                    select
                        r.id as role_id,
                        r.name as role_name
                    from t_user_role ur
                             left join t_user u on ur.user_id = u.id
                             left join t_role r on ur.role_id = r.id
                    where 1 = 1
                    and u.username = ?
                    """);
            return jdbcTemplate.queryForList(sql.toString(), List.of(userName).toArray());
        } else {
            sql.append("""
                        select r.id as role_id, r.name as role_name
                        from t_role r;
                    """);
            return jdbcTemplate.queryForList(sql.toString());
        }
    }

    /**
     * 根据用户ID获取用户详细信息（包括角色信息）
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    public List<Map<String, Object>> getUserInfoById(Integer userId) {
        return jdbcTemplate.queryForList("""
                select r.* from t_user u
                left join t_user_role ur on u.id = ur.user_id
                left join t_role r on ur.role_id = r.id
                where u.id = ?
                """, List.of(userId).toArray());
    }
}