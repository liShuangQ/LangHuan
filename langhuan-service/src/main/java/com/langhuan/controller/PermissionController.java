package com.langhuan.controller;

import com.langhuan.common.Result;
import com.langhuan.model.domain.TPermission;
import com.langhuan.service.TPermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/permission")
public class PermissionController {
    private final TPermissionService TPermissionService;

    public PermissionController(TPermissionService TPermissionService) {
        this.TPermissionService = TPermissionService;
    }

    @PostMapping("/add")
    public Result add(@RequestBody TPermission role) {
        return Result.success(TPermissionService.add(role));
    }

    @PreAuthorize("hasAuthority('/user/manager')")
    @PostMapping("/delete")
    public Result delete(@RequestParam(name = "id", required = true) Integer id) throws AuthorizationDeniedException {
        Boolean delete = TPermissionService.delete(id);
        if (!delete) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    @PreAuthorize("hasAuthority('/user/manager')")
    @PostMapping("/change")
    public Result change(@RequestBody TPermission role) throws AuthorizationDeniedException {
        return Result.success(TPermissionService.change(role));
    }

    @PostMapping("/getPageList")
    public Result getPageList(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "url", required = false, defaultValue = "") String url,
            @RequestParam(name = "parentId", required = false) Integer parentId,
            @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        return Result.success(TPermissionService.getPageList(name, url, parentId, pageNum, pageSize));
    }

}
