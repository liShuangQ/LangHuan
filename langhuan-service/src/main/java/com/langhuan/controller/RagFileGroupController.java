package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.service.TRagFileGroupService;
import com.langhuan.service.TRagFileService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/rag/file-group")
public class RagFileGroupController {
    private final TRagFileGroupService ragFileGroupService;
    private final TRagFileService ragFileService;

    public RagFileGroupController(TRagFileGroupService ragFileGroupService, TRagFileService ragFileService) {
        this.ragFileGroupService = ragFileGroupService;
        this.ragFileService = ragFileService;
    }

    // @PreAuthorize("hasAuthority('/rag/file-group/getEnum')")
    @PostMapping("/getEnum")
    public Result getEnum() {
        return Result.success(ragFileGroupService.getEnum());
    }

    @PreAuthorize("hasAuthority('/rag/file-group/add')")
    @PostMapping("/add")
    public Result addFileGroup(@Valid @RequestBody TRagFileGroup fileGroup) {
        log.info("Adding new file group: {}", fileGroup);
        if (fileGroup.getGroupName() == null || fileGroup.getGroupType() == null) {
            return Result.error("文件组名称和文件组类型不能为空");
        }
        fileGroup.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
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
        boolean success = ragFileGroupService.updateById(fileGroup);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('/rag/file-group/query')")
    @PostMapping("/query")
    public Result queryFileGroups(@RequestParam(required = false) String groupName,
            @RequestParam(required = false) String groupType,
            @RequestParam int pageNum,
            @RequestParam int pageSize) {
        log.info("Querying file groups with groupName: {}, groupType: {}, page: {}, size: {}", groupName, groupType,
                pageNum, pageSize);
        if (groupName == null && groupType == null) {
            return Result.error("至少需要提供文件组名称或文件组类型");
        }
        return Result.success(ragFileGroupService.queryFileGroups(groupName, groupType, pageNum, pageSize));
    }

}
