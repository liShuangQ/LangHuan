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
     * 获取反馈统计信息
     * 包括总数、按类型统计、按用户统计
     * 
     * @return 反馈统计结果
     */
    @PostMapping("/feedbackStats")
    public Result<Map<String, Object>> getFeedbackStats() {
        return Result.success(dashboardService.getFeedbackStats());
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

}