package com.langhuan.controller

import com.langhuan.common.Result
import com.langhuan.model.domain.TPermission
import com.langhuan.service.TPermissionService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/permission"])
class PermissionController(private val tPermissionService: TPermissionService) {

    companion object {
        private val log = LoggerFactory.getLogger(PermissionController::class.java)
    }

    @PreAuthorize("hasAuthority('/permission/add')")
    @PostMapping("/add")
    fun add(@RequestBody role: TPermission): Result<*> {
        return Result.success(tPermissionService.add(role))
    }

    @PreAuthorize("hasAuthority('/permission/delete')")
    @PostMapping("/delete")
    @Throws(AuthorizationDeniedException::class)
    fun delete(@RequestParam(name = "id", required = true) id: Int): Result<Any> {
        val delete = tPermissionService.delete(id)
        if (!delete) {
            return Result.error("删除失败")
        }
        return Result.success("删除成功")
    }

    @PreAuthorize("hasAuthority('/permission/change')")
    @PostMapping("/change")
    @Throws(AuthorizationDeniedException::class)
    fun change(@RequestBody role: TPermission): Result<*> {
        return Result.success(tPermissionService.change(role))
    }

    @PreAuthorize("hasAuthority('/permission/list')")
    @PostMapping("/getPageList")
    fun getPageList(
        @RequestParam(name = "name", required = false, defaultValue = "") name: String,
        @RequestParam(name = "url", required = false, defaultValue = "") url: String,
        @RequestParam(name = "parentId", required = false) parentId: Int?,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") pageNum: Int,
        @RequestParam(name = "pageSize", required = false, defaultValue = "10") pageSize: Int
    ): Result<*> {
        return Result.success(tPermissionService.getPageList(name, url, parentId, pageNum, pageSize))
    }
}
