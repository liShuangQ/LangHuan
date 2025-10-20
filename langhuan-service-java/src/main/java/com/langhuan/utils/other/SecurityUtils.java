package com.langhuan.utils.other;

import com.langhuan.model.pojo.AccountUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtils {

    /**
     * 获取当前登录用户的角色ID列表
     * 
     * @return 角色ID列表
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // 去掉ROLE_前缀，得到角色ID
                .collect(Collectors.toList());
    }

    /**
     * 获取当前登录用户的ID
     * 
     * @return 用户ID
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AccountUser) {
            AccountUser user = (AccountUser) authentication.getPrincipal();
            return user.getUserId();
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户名
     * 
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 检查当前用户是否有指定角色ID
     * 
     * @param roleId 角色ID（不需要ROLE_前缀）
     * @return 是否有该角色ID
     */
    public static boolean hasRole(String roleId) {
        List<String> roles = getCurrentUserRoles();
        return roles.contains(roleId);
    }

    /**
     * 检查当前用户是否有任意一个指定角色ID
     * 
     * @param roleIds 角色ID列表（不需要ROLE_前缀）
     * @return 是否有任意一个角色ID
     */
    public static boolean hasAnyRole(String... roleIds) {
        List<String> userRoles = getCurrentUserRoles();
        return List.of(roleIds).stream().anyMatch(userRoles::contains);
    }

    /**
     * 检查当前用户是否有管理员角色
     * 
     * @return 是否有管理员角色
     */
    public static boolean hasAdminRole() {
        List<String> roles = getCurrentUserRoles();
        return roles.contains("1");
    }
}