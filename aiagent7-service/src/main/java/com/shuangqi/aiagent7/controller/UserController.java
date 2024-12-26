package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.model.domain.TUser;
import com.shuangqi.aiagent7.model.dto.UserLoginDTO;
import com.shuangqi.aiagent7.service.UserService;
import com.shuangqi.aiagent7.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/user")
public class UserController {
    private final JwtUtil jwtUtil;

    private final UserService userService;

    public UserController(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result register(@RequestBody TUser user) {
        return Result.success(userService.register(user));
    }

    @PostMapping("/login")
    public Result login(@RequestBody @Validated UserLoginDTO userLoginDTO, HttpServletResponse response) {
        return Result.success(userService.login(userLoginDTO, response));
    }

    @PostMapping("/getUserInfoByToken")
    public Result getUserInfoByToken(
    ) {
        return Result.success(userService.getUserInfoByToken());
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

        return Result.success(userService.getUserPageList(name, username, gender, enabled, currentPage, pageSize));
    }


    //@PreAuthorize配合@EnableGlobalMethodSecurity(prePostEnabled = true)使用
    //@PreAuthorize("hasAuthority('/user/list')")
    //@PreAuthorize("hasAnyRole('admin', 'normal')")
    //@PreAuthorize("hasRole('/user/manager1')") //具有xx权限才支持这个接口
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        // 退出登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 清除认证
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return Result.success("操作成功");
    }
}