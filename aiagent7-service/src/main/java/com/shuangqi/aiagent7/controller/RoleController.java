package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.common.Result;
import com.shuangqi.aiagent7.model.domain.TRole;
import com.shuangqi.aiagent7.service.RoleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/role")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }


    @PostMapping("/add")
    public Result add(@RequestBody TRole role) {
        return Result.success(roleService.add(role));
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam(name = "id", required = true) Integer id) {
        Boolean delete = roleService.delete(id);
        if (!delete) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @PostMapping("/change")
    public Result change(@RequestBody TRole role) {
        return Result.success(roleService.change(role));
    }

    @PostMapping("/getPageList")
    public Result getPageList(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "remark", required = false, defaultValue = "") String remark,
            @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        return Result.success(roleService.getPageList(name, remark, currentPage, pageSize));
    }

}
