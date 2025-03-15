package com.langhuan.controller;

import cn.hutool.core.util.IdUtil;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.service.TRagFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/rag/file")
public class RagFileController {
    private final TRagFileService ragFileService;

    public RagFileController(TRagFileService ragFileService) {
        this.ragFileService = ragFileService;
    }


    @PostMapping("/delete")
    public Result deleteFile(@RequestParam Long id) {
        log.info("Deleting file with ID: {}", id);
        boolean success = ragFileService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PostMapping("/update")
    public Result updateFile(@Valid @RequestBody TRagFile ragFile) {
        log.info("Updating file: {}", ragFile);
        boolean success = ragFileService.updateById(ragFile);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PostMapping("/query")
    public Result queryFiles(@RequestParam(required = false) String fileName,
                             @RequestParam(required = false) String fileType,
                             @RequestParam int pageNum,
                             @RequestParam int pageSize) {
        log.info("Querying file with fileName: {}, fileType: {}, page: {}, size: {}", fileName, fileType, pageNum, pageSize);
        return Result.success(ragFileService.queryFiles(fileName, fileType, pageNum, pageSize));
    }
}
