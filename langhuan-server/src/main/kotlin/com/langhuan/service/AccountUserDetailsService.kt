package com.langhuan.service

import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.langhuan.model.domain.TUser
import com.langhuan.model.pojo.AccountUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AccountUserDetailsService(private val TUserService: TUserService) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = TUserService.getOne(Wrappers.query<TUser>().eq("username", username), true)
            ?: throw UsernameNotFoundException("用户名或密码错误")
        return AccountUser(
            user.id as Integer,
            user.username,
            user.password,
            getUserAuthority(user.username as String) as MutableCollection<out GrantedAuthority>
        )
    }

    /**
     * 获取用户权限信息（角色、菜单权限）
     *
     * @param username
     * @return
     */
    fun getUserAuthority(username: String): List<GrantedAuthority> {
        // 角色(比如ROLE_admin)，菜单操作权限(比如sys:user:list)
        // 角色必须以ROLE_开头，security在判断角色时会自动截取ROLE_
        // 比如ROLE_admin,ROLE_normal,sys:user:list,...

        // 使用；
        // @PreAuthorize配合@EnableMethodSecurity(prePostEnabled = true)使用
        // @PreAuthorize("hasAuthority('/user/list')")
        // @PreAuthorize("hasAnyRole('admin', 'normal')")

        val authorities = mutableListOf<String>()

        // 获取用户权限
        val permissions = TUserService.getPermissionByUsername(username)
        if (CollectionUtils.isNotEmpty(permissions)) {
            val urls = permissions.mapNotNull { it.url }
            authorities.addAll(urls)
        }

        // 获取用户角色，并添加ROLE_前缀（使用role_id作为权限标识）
        val userRolesMap = TUserService.getUserRoles(username)
        if (CollectionUtils.isNotEmpty(userRolesMap)) {
            val roleAuthorities = userRolesMap.map { "ROLE_" + it["role_id"].toString() }
            authorities.addAll(roleAuthorities)
        }

        return AuthorityUtils.commaSeparatedStringToAuthorityList(
            StrUtil.join(",", authorities)
        )
    }
}
