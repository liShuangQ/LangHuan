package com.langhuan.controller

import com.langhuan.common.Result
import com.langhuan.model.domain.TRole
import com.langhuan.service.TRoleService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/role"])
class RoleController(private val tRoleService: TRoleService) {

    companion object {
        private val log = LoggerFactory.getLogger(RoleController::class.java)
    }

    @PreAuthorize("hasAuthority('/role/add')")
    @PostMapping("/add")
    fun add(@RequestBody role: TRole): Result<*> {
        return Result.success(tRoleService.add(role))
    }

    @PreAuthorize("hasAuthority('/role/delete')")
    @PostMapping("/delete")
    @Throws(AuthorizationDeniedException::class)
    fun delete(@RequestParam(name = "id", required = true) id: Int): Result<String> {
        if (id == 1) {
            return Result.error("管理员角色不能删除")
        }
        val delete = tRoleService.delete(id)
        if (!delete) {
            return Result.error("删除失败")
        }
        return Result.success("删除成功")
    }

    @PreAuthorize("hasAuthority('/role/change')")
    @PostMapping("/change")
    @Throws(AuthorizationDeniedException::class)
    fun change(@RequestBody role: TRole): Result<*> {
        return Result.success(tRoleService.change(role))
    }

    // @PreAuthorize("hasAuthority('/role/list')")
    @PostMapping("/getPageList")
    fun getPageList(
        @RequestParam(name = "name", required = false, defaultValue = "") name: String,
        @RequestParam(name = "remark", required = false, defaultValue = "") remark: String,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") pageNum: Int,
        @RequestParam(name = "pageSize", required = false, defaultValue = "10") pageSize: Int
    ): Result<*> {
        return Result.success(tRoleService.getPageList(name, remark, pageNum, pageSize))
    }

    @PreAuthorize("hasAuthority('/role/permission/view')")
    @PostMapping("/getRolePermission")
    fun getRolePermission(@RequestParam(name = "id", required = false) id: Int?): Result<*> {
        return Result.success(tRoleService.getRolePermission(id as Int))
    }

    @PreAuthorize("hasAuthority('/role/permission/edit')")
    @PostMapping("/relevancyRoles")
    fun relevancyRoles(
        @RequestParam(name = "id", required = true) id: Int,
        @RequestParam(name = "permissionIds", required = true) permissionIds: String
    ): Result<String> {
        val auth = SecurityContextHolder.getContext().authentication

        val permissionList: List<Int>
        try {
            permissionList = if (permissionIds.isEmpty()) {
                return Result.error("权限id不能为空")
            } else {
                val strings = permissionIds.split(",").toTypedArray()
                strings.map { it.toInt() }
            }
        } catch (e: Exception) {
            return Result.error("权限id格式错误")
        }

        if (id == 1) {
            val nowRolePermission = tRoleService.getRolePermission(id)
            if (nowRolePermission.size >= permissionList.size) {
                return Result.error("管理员角色不能降低权限")
            }
        }
        tRoleService.relevancyPermissions(id, permissionList)
        return Result.success("操作成功")
    }
}
