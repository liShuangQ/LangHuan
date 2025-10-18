package com.langhuan.utils.other

import com.langhuan.model.pojo.AccountUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {

    /**
     * 获取当前登录用户的角色ID列表
     *
     * @return 角色ID列表
     */
    fun getCurrentUserRoles(): List<String> {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return emptyList()
        }

        return authentication.authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter { auth: String -> auth.startsWith("ROLE_") }
            .map { auth: String -> auth.substring(5) } // 去掉ROLE_前缀，得到角色ID
            .collect(java.util.stream.Collectors.toList())
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID
     */
    fun getCurrentUserId(): Integer? {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.principal is AccountUser) {
            val user: AccountUser = authentication.principal as AccountUser
            return user.getUserId();
        }
        return null
    }

    /**
     * 获取当前登录用户的用户名
     *
     * @return 用户名
     */
    fun getCurrentUsername(): String? {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return authentication?.name
    }

    /**
     * 检查当前用户是否有指定角色ID
     *
     * @param roleId 角色ID（不需要ROLE_前缀）
     * @return 是否有该角色ID
     */
    fun hasRole(roleId: String): Boolean {
        val roles: List<String> = getCurrentUserRoles()
        return roles.contains(roleId)
    }

    /**
     * 检查当前用户是否有任意一个指定角色ID
     *
     * @param roleIds 角色ID列表（不需要ROLE_前缀）
     * @return 是否有任意一个角色ID
     */
    fun hasAnyRole(vararg roleIds: String): Boolean {
        val userRoles: List<String> = getCurrentUserRoles()
        return listOf(*roleIds).any { userRoles.contains(it) }
    }

    /**
     * 检查当前用户是否有管理员角色
     *
     * @return 是否有管理员角色
     */
    fun hasAdminRole(): Boolean {
        val roles: List<String> = getCurrentUserRoles()
        return roles.contains("1")
    }
}
