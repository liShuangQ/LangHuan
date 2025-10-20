package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.domain.TRagFileGroupShare;
import com.langhuan.service.TRagFileGroupService;
import com.langhuan.service.TRagFileGroupShareService;
import com.langhuan.service.TRagFileService;
import com.langhuan.utils.other.SecurityUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/rag/file-group")
public class RagFileGroupController {
    private final TRagFileGroupService ragFileGroupService;
    private final TRagFileService ragFileService;
    private final TRagFileGroupShareService ragFileGroupShareService;

    public RagFileGroupController(TRagFileGroupService ragFileGroupService,
            TRagFileService ragFileService,
            TRagFileGroupShareService ragFileGroupShareService) {
        this.ragFileGroupService = ragFileGroupService;
        this.ragFileService = ragFileService;
        this.ragFileGroupShareService = ragFileGroupShareService;
    }

    // @PreAuthorize("hasAuthority('/rag/file-group/getEnum')")
    @PostMapping("/getEnum")
    public Result getEnum(
            @RequestParam(defaultValue = "true") Boolean isRead) {
        return Result.success(ragFileGroupService.getEnum(isRead));
    }

    @PreAuthorize("hasAuthority('/rag/file-group/add')")
    @PostMapping("/add")
    public Result addFileGroup(@Valid @RequestBody TRagFileGroup fileGroup) {
        log.info("Adding new file group: {}", fileGroup);
        if (fileGroup.getGroupName() == null || fileGroup.getGroupType() == null) {
            return Result.error("文件组名称和文件组类型不能为空");
        }

        // 设置创建者
        fileGroup.setCreatedBy(SecurityUtils.getCurrentUsername());

        // 如果没有设置visibility，默认为private
        if (fileGroup.getVisibility() == null || fileGroup.getVisibility().trim().isEmpty()) {
            fileGroup.setVisibility("private");
        }

        // 验证visibility值
        if (!"private".equals(fileGroup.getVisibility()) && !"public".equals(fileGroup.getVisibility())) {
            return Result.error("文件组可见性只能是private或public");
        }

        boolean success = ragFileGroupService.save(fileGroup);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PreAuthorize("hasAuthority('/rag/file-group/delete')")
    @PostMapping("/delete")
    public Result deleteFileGroup(@RequestParam Long id) {
        long count = ragFileService.count(
                new LambdaQueryWrapper<TRagFile>()
                        .eq(TRagFile::getFileGroupId, id.toString()));
        if (count > 0) {
            return Result.error("文件组下存在文件，无法删除");
        }
        log.info("Deleting file group with ID: {}", id);
        boolean success = ragFileGroupService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PreAuthorize("hasAuthority('/rag/file-group/update')")
    @PostMapping("/update")
    public Result updateFileGroup(@Valid @RequestBody TRagFileGroup fileGroup) {
        log.info("Updating file group: {}", fileGroup);
        if (fileGroup.getId() == null) {
            return Result.error("文件组ID不能为空");
        }

        // 检查权限：只有管理员或创建者可以修改
        TRagFileGroup existingGroup = ragFileGroupService.getById(fileGroup.getId());
        if (existingGroup == null) {
            return Result.error("文件组不存在");
        }

        String currentUser = SecurityUtils.getCurrentUsername();
        boolean isAdmin = SecurityUtils.hasAdminRole();

        if (!isAdmin && !currentUser.equals(existingGroup.getCreatedBy())) {
            return Result.error("无权限修改此文件组");
        }

        if (fileGroup.getVisibility() != null && fileGroup.getVisibility() == "private"
                && "public".equals(existingGroup.getVisibility())) {
            return Result.error("公开文件组不能修改为私有");
        }

        // 验证visibility值
        if (fileGroup.getVisibility() != null &&
                !"private".equals(fileGroup.getVisibility()) &&
                !"public".equals(fileGroup.getVisibility())) {
            return Result.error("文件组可见性只能是private或public");
        }
        // 修改不改变原来的创建者
        fileGroup.setCreatedBy(existingGroup.getCreatedBy());

        boolean success = ragFileGroupService.updateById(fileGroup);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('/rag/file-group/query')")
    @PostMapping("/query")
    public Result queryFileGroups(@RequestParam(required = false) String groupName,
            @RequestParam(required = false) String groupType,
            @RequestParam(required = false) String visibility,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        log.info("Querying file groups with groupName: {}, groupType: {}, visibility: {}, page: {}, size: {}",
                groupName, groupType, visibility,
                pageNum, pageSize);

        return Result.success(ragFileGroupService.queryFileGroups(groupName, groupType, visibility, pageNum, pageSize));
    }

    /**
     * 分享文件组给指定用户（支持单个或多个用户，多个时用逗号分割）
     */
    // @PreAuthorize("hasAuthority('/rag/file-group/share')")
    @PostMapping("/share")
    public Result shareFileGroup(@RequestParam Integer fileGroupId,
            @RequestParam String sharedWith,
            @RequestParam(defaultValue = "false") Boolean canRead,
            @RequestParam(defaultValue = "false") Boolean canAdd,
            @RequestParam(defaultValue = "false") Boolean canUpdate,
            @RequestParam(defaultValue = "false") Boolean canDelete) {
        log.info("Sharing file group {} to users {} with permissions: read={}, add={}, update={}, delete={}",
                fileGroupId, sharedWith, canRead, canAdd, canUpdate, canDelete);

        // 检查文件组是否存在
        TRagFileGroup fileGroup = ragFileGroupService.getById(fileGroupId);
        if (fileGroup == null) {
            return Result.error("文件组不存在");
        }

        // 检查权限：只有管理员或创建者可以分享
        String currentUser = SecurityUtils.getCurrentUsername();
        boolean isAdmin = SecurityUtils.hasAdminRole();

        if (!isAdmin && !currentUser.equals(fileGroup.getCreatedBy())) {
            return Result.error("无权限分享此文件组");
        }

        // 解析逗号分隔的用户列表
        List<String> userList = Arrays.asList(sharedWith.split(","))
                .stream()
                .map(String::trim)
                .filter(user -> !user.isEmpty())
                .collect(Collectors.toList());

        if (userList.isEmpty()) {
            return Result.error("分享用户不能为空");
        }

        // 检查是否包含自己
        if (userList.contains(currentUser)) {
            return Result.error("不能分享给自己");
        }

        // 执行批量分享
        int successCount = ragFileGroupShareService.shareFileGroupBatch(
                fileGroupId, userList, canRead, canAdd, canUpdate, canDelete);

        if (successCount > 0) {
            if (userList.size() == 1) {
                return Result.success("分享成功");
            } else {
                return Result.success(String.format("成功分享给%d个用户", successCount));
            }
        } else {
            return Result.error("分享失败");
        }
    }

    /**
     * 取消分享文件组（支持单个或多个用户，多个时用逗号分割）
     */
    // @PreAuthorize("hasAuthority('/rag/file-group/unshare')")
    @PostMapping("/unshare")
    public Result unshareFileGroup(@RequestParam Integer fileGroupId,
            @RequestParam String sharedWith) {
        log.info("Unsharing file group {} from users {}", fileGroupId, sharedWith);

        // 检查文件组是否存在
        TRagFileGroup fileGroup = ragFileGroupService.getById(fileGroupId);
        if (fileGroup == null) {
            return Result.error("文件组不存在");
        }

        // 检查权限：只有管理员或创建者可以取消分享
        String currentUser = SecurityUtils.getCurrentUsername();
        boolean isAdmin = SecurityUtils.hasAdminRole();

        if (!isAdmin && !currentUser.equals(fileGroup.getCreatedBy())) {
            return Result.error("无权限取消分享此文件组");
        }

        // 解析逗号分隔的用户列表
        List<String> userList = Arrays.asList(sharedWith.split(","))
                .stream()
                .map(String::trim)
                .filter(user -> !user.isEmpty())
                .collect(Collectors.toList());

        if (userList.isEmpty()) {
            return Result.error("分享用户不能为空");
        }

        // 执行取消分享
        int successCount = ragFileGroupShareService.unshareFileGroup(fileGroupId, userList);
        if (successCount > 0) {
            if (userList.size() == 1) {
                return Result.success("取消分享成功");
            } else {
                return Result.success(String.format("成功取消分享给%d个用户", successCount));
            }
        } else {
            return Result.error("取消分享失败");
        }
    }

    /**
     * 获取文件组的分享列表
     */
    // @PreAuthorize("hasAuthority('/rag/file-group/shareList')")
    @PostMapping("/shareList")
    public Result getFileGroupShares(@RequestParam(defaultValue = "true") Integer fileGroupId,
            @RequestParam(defaultValue = "") String sharedWith) {
        log.info("Getting share list for file group {}", fileGroupId);

        // 检查文件组是否存在
        TRagFileGroup fileGroup = ragFileGroupService.getById(fileGroupId);
        if (fileGroup == null) {
            return Result.error("文件组不存在");
        }

        // 检查权限：只有管理员或创建者可以查看分享列表
        String currentUser = SecurityUtils.getCurrentUsername();
        boolean isAdmin = SecurityUtils.hasAdminRole();

        if (!isAdmin && !currentUser.equals(fileGroup.getCreatedBy())) {
            return Result.error("无权限查看此文件组的分享列表");
        }

        return Result.success(ragFileGroupShareService.getFileGroupShares(fileGroupId, sharedWith));
    }

}
