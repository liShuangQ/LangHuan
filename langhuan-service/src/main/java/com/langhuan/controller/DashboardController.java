package com.langhuan.controller;

import com.langhuan.common.Result;
import com.langhuan.service.DashboardService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表盘控制器
 * 提供系统统计信息相关接口
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取提示词统计信息
     * 
     * @return 提示词统计结果
     */
    @PostMapping("/promptsStats")
    public Result<Map<String, Object>> getPromptsStats() {
        return Result.success(dashboardService.getPromptsStats());
    }

    /**
     * 获取文件组统计信息
     * 包括总数和按类型统计
     * 
     * @return 文件组统计结果
     */
    @PostMapping("/fileGroupStats")
    public Result<Map<String, Object>> getFileGroupStats() {
        return Result.success(dashboardService.getFileGroupStats());
    }

    /**
     * 获取文件统计信息
     * 包括文件数量、文件总大小、文件切割数总和、上传用户数
     * 
     * @return 文件统计结果
     */
    @PostMapping("/fileStats")
    public Result<Map<String, Object>> getFileStats() {
        return Result.success(dashboardService.getFileStats());
    }

    /**
     * 获取用户统计信息
     * 包括用户总数、性别分布、性别比例
     * 
     * @return 用户统计结果
     */
    @PostMapping("/userStats")
    public Result<Map<String, Object>> getUserStats() {
        return Result.success(dashboardService.getUserStats());
    }

    /**
     * 获取使用情况统计
     * 包括总提问次数、本周各用户提问统计等
     * 统计信息包含：用户名、提问次数、有效回答数、无效回答数、问题反馈数
     * 
     * @return 使用情况统计结果
     */
    @PostMapping("/usageStats")
    public Result<Map<String, Object>> getUsageStats() {
        return Result.success(dashboardService.getUsageStats());
    }

}