package com.langhuan.controller;

import com.langhuan.common.Result;
import com.langhuan.model.domain.TUser;
import com.langhuan.model.dto.UserLoginDTO;
import com.langhuan.service.TUserService;
import com.langhuan.utils.other.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/user")
public class UserController {
    private final JwtUtil jwtUtil;

    private final TUserService TUserService;

    public UserController(JwtUtil jwtUtil, TUserService TUserService) {
        this.jwtUtil = jwtUtil;
        this.TUserService = TUserService;
    }

    @PostMapping("/register")
    public Result register(@RequestBody TUser user) {
        return Result.success(TUserService.register(user));
    }

    @PreAuthorize("hasRole('/user/manager')")
    @PostMapping("/change")
    public Result change(@RequestBody TUser user) throws AuthorizationDeniedException {
        return Result.success(TUserService.change(user));
    }

    @PostMapping("/login")
    public Result login(@RequestBody @Validated UserLoginDTO userLoginDTO, HttpServletResponse response) {
        return Result.success(TUserService.login(userLoginDTO, response));
    }

    @PostMapping("/getUserInfoByToken")
    public Result getUserInfoByToken(
    ) {
        return Result.success(TUserService.getUserInfoByToken());
    }

    @PostMapping("/getUserPageList")
    public Result getUserPageList(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "username", required = false, defaultValue = "") String username,
            @RequestParam(name = "gender", required = false) Integer gender,
            @RequestParam(name = "enabled", required = false) Integer enabled,
            @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {

        return Result.success(TUserService.getUserPageList(name, username, gender, enabled, currentPage, pageSize));
    }

    @PreAuthorize("hasRole('/user/manager')")
    @PostMapping("/delete")
    public Result delete(@RequestParam(name = "id", required = true) Integer id) throws AuthorizationDeniedException {
        Boolean delete = TUserService.delete(id);
        if (!delete) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @PostMapping("/getUserRoles")
    public Result getUserRoles(@RequestParam(name = "id", required = false) Integer id) {
        return Result.success(TUserService.getUserRoles(id));
    }

    @PostMapping("/relevancyRoles")
    public Result relevancyRoles(
            @RequestParam(name = "id", required = true) Integer id,
            @RequestParam(name = "roleIds", required = true) String roleIds
    ) {
        try {
            if (roleIds.isEmpty()) {
                TUserService.relevancyRoles(id, new ArrayList<>());
            } else {
                String[] strings = roleIds.split(",");
                TUserService.relevancyRoles(id, Arrays.stream(strings).map(Integer::parseInt).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            return Result.error("角色id格式错误");
        }
        return Result.success("操作成功");
    }


    //@PreAuthorize("hasAuthority('/user/list')")
    //@PreAuthorize("hasAnyRole('admin', 'normal')")
//    @PreAuthorize("hasRole('/user/manager')") //具有xx权限才支持这个接口
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        // 退出登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 清除认证
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return Result.success("操作成功");
    }
}