package com.langhuan.controller

import com.langhuan.common.ApiLog
import com.langhuan.common.Result
import com.langhuan.model.domain.TUser
import com.langhuan.model.dto.UserLoginDTO
import com.langhuan.service.TUserService
import com.langhuan.utils.other.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/user"])
class UserController(
    private val jwtUtil: JwtUtil,
    private val tUserService: TUserService
) {

    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

    // @PreAuthorize("hasAuthority('/user/register')")
    @PostMapping("/register")
    fun register(@RequestBody user: TUser): Result<Any> {
        return Result.success(tUserService.register(user))
    }

    @PreAuthorize("hasAuthority('/user/change')")
    @PostMapping("/change")
    @Throws(AuthorizationDeniedException::class)
    fun change(@RequestBody user: TUser): Result<Any> {
        return Result.success(tUserService.change(user))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Validated userLoginDTO: UserLoginDTO, response: HttpServletResponse): Result<Any> {
        return Result.success(tUserService.login(userLoginDTO, response))
    }

    // @PreAuthorize("hasAuthority('/user/info/view')")
    @PostMapping("/getUserInfoByToken")
    fun getUserInfoByToken(): Result<Any> {
        return Result.success(tUserService.getUserInfoByToken())
    }

    @PreAuthorize("hasAuthority('/user/list')")
    @PostMapping("/getUserPageList")
    fun getUserPageList(
        @RequestParam(name = "name", required = false, defaultValue = "") name: String,
        @RequestParam(name = "username", required = false, defaultValue = "") username: String,
        @RequestParam(name = "gender", required = false) gender: Int?,
        @RequestParam(name = "enabled", required = false) enabled: Int?,
        @RequestParam(name = "pageNum", required = true, defaultValue = "1") pageNum: Int,
        @RequestParam(name = "pageSize", required = true, defaultValue = "10") pageSize: Int
    ): Result<Any> {
        return Result.success(tUserService.getUserPageList(name, username, gender, enabled, pageNum, pageSize))
    }

    @ApiLog(apiName = "删除用户", description = "根据用户ID删除用户信息")
    @PreAuthorize("hasAuthority('/user/delete')")
    @PostMapping("/delete")
    @Throws(AuthorizationDeniedException::class)
    fun delete(@RequestParam(name = "id", required = true) id: Int): Result<Any> {
        if (id == 1) {
            return Result.error("管理员用户无法删除")
        }
        val delete = tUserService.delete(id)
        if (!delete) {
            return Result.error("删除失败")
        }
        return Result.success("删除成功")
    }

    @PreAuthorize("hasAuthority('/user/roles/view')")
    @PostMapping("/getUserRoles")
    fun getUserRoles(@RequestParam(name = "id", required = false) id: Int?): Result<Any> {
        return Result.success(tUserService.getUserRoles(id))
    }

    @PreAuthorize("hasAuthority('/user/roles/edit')")
    @PostMapping("/relevancyRoles")
    fun relevancyRoles(
        @RequestParam(name = "id", required = true) id: Int,
        @RequestParam(name = "roleIds", required = true) roleIds: String
    ): Result<Any> {
        try {
            if (roleIds.isEmpty()) {
                tUserService.relevancyRoles(id, ArrayList())
            } else {
                val strings = roleIds.split(",").toTypedArray()
                tUserService.relevancyRoles(
                    id,
                    strings.map { it.toInt() }
                )
            }
        } catch (e: Exception) {
            return Result.error("角色id格式错误")
        }
        return Result.success("操作成功")
    }

    // @PreAuthorize("hasAuthority('/user/list')")
    // @PreAuthorize("hasAnyRole('admin', 'normal')")
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): Result<Any> {
        // 退出登录
        val auth = SecurityContextHolder.getContext().authentication
        // 清除认证
        SecurityContextLogoutHandler().logout(request, response, auth)
        return Result.success("操作成功")
    }

    // @PreAuthorize("hasAuthority('/user/list')")
    @PostMapping("/getUserRoleListById")
    fun getUserRoleListById(@RequestParam(name = "roleId", required = false) roleId: Int?): Result<Any> {
        return Result.success(tUserService.getUserRoleListById(roleId as Int))
    }
}
