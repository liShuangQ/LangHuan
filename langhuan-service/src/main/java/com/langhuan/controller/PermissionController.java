package com.langhuan.controller;

import com.langhuan.model.domain.TPermission;
import com.langhuan.common.Result;
import com.langhuan.service.PermissionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/permission")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/add")
    public Result add(@RequestBody TPermission role) {
        return Result.success(permissionService.add(role));
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam(name = "id", required = true) Integer id) {
        Boolean delete = permissionService.delete(id);
        if (!delete) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @PostMapping("/change")
    public Result change(@RequestBody TPermission role) {
        return Result.success(permissionService.change(role));
    }

    @PostMapping("/getPageList")
    public Result getPageList(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "url", required = false, defaultValue = "") String url,
            @RequestParam(name = "parentId", required = false) Integer parentId,
            @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        return Result.success(permissionService.getPageList(name, url, parentId, currentPage, pageSize));
    }

}
