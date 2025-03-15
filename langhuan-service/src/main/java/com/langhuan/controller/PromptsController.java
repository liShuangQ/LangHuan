package com.langhuan.controller;

import com.langhuan.common.Constant;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TPrompts;
import com.langhuan.service.TPromptsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prompts")
public class PromptsController {

    @Autowired
    private TPromptsService tPromptsService;

    // 提示词分类的枚举
    // NOTE：这里后续可能改动很小，直接在程序中定义死即可
    @PostMapping("/usePrompt/getCategoryEnum")
    public Result getCategoryEnum(@RequestBody TPrompts tPrompts) {
        return Result.success(Constant.CATEGORYENUM);
    }

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
