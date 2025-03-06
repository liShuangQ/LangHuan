package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.model.domain.TPrompts;
import com.shuangqi.aiagent7.service.TPromptsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prompts")
public class PromptsController {

    @Autowired
    private TPromptsService tPromptsService;

    // 新增
    @PostMapping("/usePrompt/add")
    public Result saveTPrompts(@RequestBody TPrompts tPrompts) {
        boolean b = tPromptsService.saveTPrompts(tPrompts);
        return b ? Result.success("操作成功") : Result.error("操作失败");

    }

    // 删除
    @PostMapping("/usePrompt/delete")
    public Result removeTPromptsById(@RequestParam Integer id) {
        boolean b = tPromptsService.removeTPromptsById(id);
        return b ? Result.success("操作成功") : Result.error("操作失败");
    }

    // 修改
    @PostMapping("/usePrompt/update")
    public Result updateTPrompts(@RequestBody TPrompts tPrompts) {
        boolean b = tPromptsService.updateTPrompts(tPrompts);
        return b ? Result.success("操作成功") : Result.error("操作失败");
    }

    // 分页查询并支持模糊搜索
    @PostMapping("/usePrompt/query")
    public Result getTPromptsByPage(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    @RequestParam(required = false) String methodName,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String description
    ) {
        return Result.success(tPromptsService.getTPromptsByPage(pageNum, pageSize, methodName, category, description));
    }
}
