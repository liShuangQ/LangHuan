package com.shuangqi.aiagent7.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.model.domain.TUser;
import com.shuangqi.aiagent7.model.dto.UserLoginDTO;
import com.shuangqi.aiagent7.service.UserService;
import com.shuangqi.aiagent7.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
        TUser user1 = userService.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, user.getUsername()));
        if (user1 != null) {
            return Result.error("用户名已存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword())); // 密码加密
        user.setEnabled(1); // 默认启用
        userService.save(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result login(@RequestBody @Validated UserLoginDTO userLoginDTO, HttpServletResponse response) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        TUser user = userService.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, userLoginDTO.getUsername()));
        if (user == null) {
            return Result.error("用户名不存在");
        }
        // HACK: 兼容初始创建的两个用户, 极大隐患
        if (user.getId().equals(1) || user.getId().equals(2)) {
            if (!user.getPassword().equals(password)) {
                return Result.error("用户名或密码错误");
            }
        } else {
            if (!new BCryptPasswordEncoder().matches(password, user.getPassword())) {
                return Result.error("用户名或密码错误");
            }
        }

//        if (!user.getPassword().equals(password)) {
//            return Result.error("用户名或密码错误");
//        }

        String token = jwtUtil.generateToken(username);
        response.setHeader(JwtUtil.HEADER, token);
        response.setHeader("Access-control-Expost-Headers", JwtUtil.HEADER);
        Map<String, String> map = new HashMap<>();
        map.put("token", token);

        return Result.success(map);
    }

    @PostMapping("/getUserInfoByToken")
    public Result getUserInfoByToken(
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TUser user = userService.getOne(new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, auth.getName()));
        user.setPassword(null);
        return Result.success(user);
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

        Page<TUser> userPage = userService.page(new Page<>(currentPage, pageSize),
                new LambdaQueryWrapper<TUser>()
                        .like(!name.isEmpty(), TUser::getName, name)
                        .like(!username.isEmpty(), TUser::getUsername, username)
                        .eq(gender != null, TUser::getGender, gender)
                        .eq(enabled != null, TUser::getEnabled, enabled)
        );
        for (TUser record : userPage.getRecords()) {
            record.setPassword(null);
        }
        return Result.success(userPage);
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