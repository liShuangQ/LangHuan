package com.langhuan.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.langhuan.model.domain.TPermission;
import com.langhuan.model.domain.TUser;
import com.langhuan.model.pojo.AccountUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class AccountUserDetailsService implements UserDetailsService {
    private final TUserService TUserService;

    public AccountUserDetailsService(TUserService TUserService) {
        this.TUserService = TUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TUser user = TUserService.getOne(Wrappers.<TUser>lambdaQuery().eq(TUser::getUsername, username), true);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return new AccountUser(user.getId(), user.getUsername(), user.getPassword(), getUserAuthority(user.getUsername()));
    }

    /**
     * 获取用户权限信息（角色、菜单权限）
     *
     * @param username
     * @return
     */
    public List<GrantedAuthority> getUserAuthority(String username) {
        // 角色(比如ROLE_admin)，菜单操作权限(比如sys:user:list)
        // 角色必须以ROLE_开头，security在判断角色时会自动截取ROLE_
        // 比如ROLE_admin,ROLE_normal,sys:user:list,...

        // 使用；
        // @PreAuthorize配合@EnableMethodSecurity(prePostEnabled = true)使用
        // @PreAuthorize("hasAuthority('/user/list')")
        // @PreAuthorize("hasAnyRole('admin', 'normal')")
        
        List<String> authorities = new ArrayList<>();
        
        // 获取用户权限
        List<TPermission> permissions = TUserService.getPermissionByUsername(username);
        if (CollectionUtils.isNotEmpty(permissions)) {
            List<String> urls = permissions.stream()
                .map(TPermission::getUrl)
                .collect(Collectors.toList());
            authorities.addAll(urls);
        }
        
        // 获取用户角色，并添加ROLE_前缀（使用role_id作为权限标识）
        List<Map<String, Object>> userRolesMap = TUserService.getUserRoles(username);
        if (CollectionUtils.isNotEmpty(userRolesMap)) {
            List<String> roleAuthorities = userRolesMap.stream()
                .map(map -> "ROLE_" + map.get("role_id").toString())
                .collect(Collectors.toList());
            authorities.addAll(roleAuthorities);
        }
        
        return AuthorityUtils.commaSeparatedStringToAuthorityList(
            StrUtil.join(",", authorities));
    }
}