package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.service.RoleService;

public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

}
