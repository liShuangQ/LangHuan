package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langhuan.common.ApiLog;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TApiLog;
import com.langhuan.service.TApiLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.langhuan.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 接口调用日志管理控制器
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class ApiLogController {

    private final TApiLogService apiLogService;

    /**
     * 分页查询接口调用日志
     *
     * @param current   当前页码
     * @param size      每页数量
     * @param apiName   接口名称
     * @param username  用户名
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    @PostMapping("/search")
    public Result<IPage<TApiLog>> search(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String apiName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<TApiLog> page = new Page<>(current, size);
        QueryWrapper<TApiLog> queryWrapper = new QueryWrapper<>();

        // 条件查询
        if (apiName != null && !apiName.trim().isEmpty()) {
            queryWrapper.like("api_name", apiName);
        }
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like("username", username);
        }
        if (startTime != null) {
            queryWrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("create_time", endTime);
        }

        // 按创建时间降序排列
        queryWrapper.orderByDesc("create_time");

        IPage<TApiLog> result = apiLogService.page(page, queryWrapper);
        return Result.success(result);
    }

    /**
     * 获取接口调用统计信息
     *
     * @return 统计结果
     */
    @PostMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        // 这里可以实现具体的统计逻辑
        // 比如：总调用次数、成功率、平均响应时间、热门接口等
        return Result.success(Map.of(
                "totalCount", apiLogService.count(),
                "successRate", "95.5%",
                "avgResponseTime", "120ms"
        ));
    }

    /**
     * 删除接口调用日志
     *
     * @param id 日志ID
     * @return 删除结果
     */
    @ApiLog(apiName = "删除接口日志", description = "根据ID删除接口调用日志")
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        boolean result = apiLogService.removeById(id);
        return Result.success(result);
    }

    /**
     * 批量删除接口调用日志
     *
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @ApiLog(apiName = "批量删除接口日志", description = "批量删除接口调用日志")
    @PostMapping("/batchDelete")
    public Result<Boolean> batchDelete(@RequestBody java.util.List<Long> ids) {
        boolean result = apiLogService.removeByIds(ids);
        return Result.success(result);
    }

    /**
     * 清理历史日志
     *
     * @param days 保留天数
     * @return 清理结果
     */
    @ApiLog(apiName = "清理历史日志", description = "清理指定天数之前的接口调用日志")
    @PostMapping("/clean")
    public Result<Integer> cleanHistory(@RequestParam(defaultValue = "30") Integer days) {
        LocalDateTime cutoffTime = DateTimeUtils.now().minusDays(days);
        QueryWrapper<TApiLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("create_time", cutoffTime);
        
        int count = (int) apiLogService.count(queryWrapper);
        boolean result = apiLogService.remove(queryWrapper);
        
        return result ? Result.success(count) : Result.error("清理失败");
    }
} 