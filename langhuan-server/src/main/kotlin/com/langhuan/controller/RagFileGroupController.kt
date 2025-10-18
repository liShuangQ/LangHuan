package com.langhuan.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.langhuan.common.Result
import com.langhuan.model.domain.TRagFile
import com.langhuan.model.domain.TRagFileGroup
import com.langhuan.service.TRagFileGroupService
import com.langhuan.service.TRagFileGroupShareService
import com.langhuan.service.TRagFileService
import com.langhuan.utils.other.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rag/file-group")
class RagFileGroupController(
    private val ragFileGroupService: TRagFileGroupService,
    private val ragFileService: TRagFileService,
    private val ragFileGroupShareService: TRagFileGroupShareService
) {

    companion object {
        private val log = LoggerFactory.getLogger(RagFileGroupController::class.java)
    }

    // @PreAuthorize("hasAuthority('/rag/file-group/getEnum')")
    @PostMapping("/getEnum")
    fun getEnum(@RequestParam(defaultValue = "true") isRead: Boolean): Result<Any> {
        return Result.success(ragFileGroupService.getEnum(isRead))
    }

    @PreAuthorize("hasAuthority('/rag/file-group/add')")
    @PostMapping("/add")
    fun addFileGroup(@Valid @RequestBody fileGroup: TRagFileGroup): Result<Any> {
        log.info("Adding new file group: {}", fileGroup)
        if (fileGroup.groupName == null || fileGroup.groupType == null) {
            return Result.error("文件组名称和文件组类型不能为空")
        }

        // 设置创建者
        fileGroup.createdBy = SecurityUtils.getCurrentUsername()

        // 如果没有设置visibility，默认为private
        if (fileGroup.visibility == null || fileGroup.visibility!!.isBlank()) {
            fileGroup.visibility = "private"
        }

        // 验证visibility值
        if (fileGroup.visibility != "private" && fileGroup.visibility != "public") {
            return Result.error("文件组可见性只能是private或public")
        }

        val success = ragFileGroupService.save(fileGroup)
        return if (success) Result.success("添加成功") else Result.error("添加失败")
    }

    @PreAuthorize("hasAuthority('/rag/file-group/delete')")
    @PostMapping("/delete")
    fun deleteFileGroup(@RequestParam id: Long): Result<Any> {
        val count = ragFileService.count(
            QueryWrapper<TRagFile>()
                .eq("fileGroupId", id.toString())
        )
        if (count > 0) {
            return Result.error("文件组下存在文件，无法删除")
        }
        log.info("Deleting file group with ID: {}", id)
        val success = ragFileGroupService.removeById(id)
        return if (success) Result.success("删除成功") else Result.error("删除失败")
    }

    @PreAuthorize("hasAuthority('/rag/file-group/update')")
    @PostMapping("/update")
    fun updateFileGroup(@Valid @RequestBody fileGroup: TRagFileGroup): Result<Any> {
        log.info("Updating file group: {}", fileGroup)
        if (fileGroup.id == null) {
            return Result.error("文件组ID不能为空")
        }

        // 检查权限：只有管理员或创建者可以修改
        val existingGroup = ragFileGroupService.getById(fileGroup.id)
        if (existingGroup == null) {
            return Result.error("文件组不存在")
        }

        val currentUser = SecurityUtils.getCurrentUsername()
        val isAdmin = SecurityUtils.hasAdminRole()

        if (!isAdmin && currentUser != existingGroup.createdBy) {
            return Result.error("无权限修改此文件组")
        }

        if (fileGroup.visibility != null && fileGroup.visibility == "private" && existingGroup.visibility == "public") {
            return Result.error("公开文件组不能修改为私有")
        }

        // 验证visibility值
        if (fileGroup.visibility != null &&
            fileGroup.visibility != "private" &&
            fileGroup.visibility != "public"
        ) {
            return Result.error("文件组可见性只能是private或public")
        }
        // 修改不改变原来的创建者
        fileGroup.createdBy = existingGroup.createdBy

        val success = ragFileGroupService.updateById(fileGroup)
        return if (success) Result.success("更新成功") else Result.error("更新失败")
    }

    @PreAuthorize("hasAuthority('/rag/file-group/query')")
    @PostMapping("/query")
    fun queryFileGroups(
        @RequestParam(required = false) groupName: String?,
        @RequestParam(required = false) groupType: String?,
        @RequestParam(required = false) visibility: String?,
        @RequestParam pageNum: Int,
        @RequestParam pageSize: Int
    ): Result<Any> {
        log.info("Querying file groups with groupName: {}, groupType: {}, visibility: {}, page: {}, size: {}",
            groupName, groupType, visibility, pageNum, pageSize)

        return Result.success(ragFileGroupService.queryFileGroups(groupName, groupType, visibility, pageNum, pageSize))
    }

    /**
     * 分享文件组给指定用户（支持单个或多个用户，多个时用逗号分割）
     */
    // @PreAuthorize("hasAuthority('/rag/file-group/share')")
    @PostMapping("/share")
    fun shareFileGroup(
        @RequestParam fileGroupId: Int,
        @RequestParam sharedWith: String,
        @RequestParam(defaultValue = "false") canRead: Boolean,
        @RequestParam(defaultValue = "false") canAdd: Boolean,
        @RequestParam(defaultValue = "false") canUpdate: Boolean,
        @RequestParam(defaultValue = "false") canDelete: Boolean
    ): Result<Any> {
        log.info("Sharing file group {} to users {} with permissions: read={}, add={}, update={}, delete={}",
            fileGroupId, sharedWith, canRead, canAdd, canUpdate, canDelete)

        // 检查文件组是否存在
        val fileGroup = ragFileGroupService.getById(fileGroupId)
        if (fileGroup == null) {
            return Result.error("文件组不存在")
        }

        // 检查权限：只有管理员或创建者可以分享
        val currentUser = SecurityUtils.getCurrentUsername()
        val isAdmin = SecurityUtils.hasAdminRole()

        if (!isAdmin && currentUser != fileGroup.createdBy) {
            return Result.error("无权限分享此文件组")
        }

        // 解析逗号分隔的用户列表
        val userList = sharedWith.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (userList.isEmpty()) {
            return Result.error("分享用户不能为空")
        }

        // 检查是否包含自己
        if (userList.contains(currentUser)) {
            return Result.error("不能分享给自己")
        }

        // 执行批量分享
        val successCount = ragFileGroupShareService.shareFileGroupBatch(
            fileGroupId, userList, canRead, canAdd, canUpdate, canDelete
        )

        return if (successCount > 0) {
            if (userList.size == 1) {
                Result.success("分享成功")
            } else {
                Result.success(String.format("成功分享给%d个用户", successCount))
            }
        } else {
            Result.error("分享失败")
        }
    }

    /**
     * 取消分享文件组（支持单个或多个用户，多个时用逗号分割）
     */
    // @PreAuthorize("hasAuthority('/rag/file-group/unshare')")
    @PostMapping("/unshare")
    fun unshareFileGroup(
        @RequestParam fileGroupId: Int,
        @RequestParam sharedWith: String
    ): Result<Any> {
        log.info("Unsharing file group {} from users {}", fileGroupId, sharedWith)

        // 检查文件组是否存在
        val fileGroup = ragFileGroupService.getById(fileGroupId)
        if (fileGroup == null) {
            return Result.error("文件组不存在")
        }

        // 检查权限：只有管理员或创建者可以取消分享
        val currentUser = SecurityUtils.getCurrentUsername()
        val isAdmin = SecurityUtils.hasAdminRole()

        if (!isAdmin && currentUser != fileGroup.createdBy) {
            return Result.error("无权限取消分享此文件组")
        }

        // 解析逗号分隔的用户列表
        val userList = sharedWith.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (userList.isEmpty()) {
            return Result.error("分享用户不能为空")
        }

        // 执行取消分享
        val successCount = ragFileGroupShareService.unshareFileGroup(fileGroupId, userList)
        return if (successCount > 0) {
            if (userList.size == 1) {
                Result.success("取消分享成功")
            } else {
                Result.success(String.format("成功取消分享给%d个用户", successCount))
            }
        } else {
            Result.error("取消分享失败")
        }
    }

    /**
     * 获取文件组的分享列表
     */
    // @PreAuthorize("hasAuthority('/rag/file-group/shareList')")
    @PostMapping("/shareList")
    fun getFileGroupShares(
        @RequestParam(defaultValue = "true") fileGroupId: Int,
        @RequestParam(defaultValue = "") sharedWith: String
    ): Result<Any> {
        log.info("Getting share list for file group {}", fileGroupId)

        // 检查文件组是否存在
        val fileGroup = ragFileGroupService.getById(fileGroupId)
        if (fileGroup == null) {
            return Result.error("文件组不存在")
        }

        // 检查权限：只有管理员或创建者可以查看分享列表
        val currentUser = SecurityUtils.getCurrentUsername()
        val isAdmin = SecurityUtils.hasAdminRole()

        if (!isAdmin && currentUser != fileGroup.createdBy) {
            return Result.error("无权限查看此文件组的分享列表")
        }

        return Result.success(ragFileGroupShareService.getFileGroupShares(fileGroupId, sharedWith))
    }
}
