package com.langhuan.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.langhuan.common.ApiLog
import com.langhuan.common.Result
import com.langhuan.model.domain.TApiLog
import com.langhuan.service.TApiLogService
import com.langhuan.utils.date.DateTimeUtils
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*

import java.time.LocalDateTime

/**
 * 接口调用日志管理控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/log")
class ApiLogController(private val apiLogService: TApiLogService) {

    companion object {
        private val log = LoggerFactory.getLogger(ApiLogController::class.java)
    }

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
    fun search(
        @RequestParam(defaultValue = "1") current: Long,
        @RequestParam(defaultValue = "10") size: Long,
        @RequestParam(required = false) apiName: String?,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") startTime: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") endTime: LocalDateTime?
    ): Result<IPage<Map<String, Any>>> {
        return Result.success(
            apiLogService.search(apiName, username, startTime, endTime, current.toInt(), size.toInt())
        )
    }

    /**
     * 获取接口调用统计信息
     *
     * @return 统计结果
     */
    @PostMapping("/statistics")
    fun getStatistics(): Result<Map<String, Any>> {
        // 这里可以实现具体的统计逻辑
        // 比如：总调用次数、成功率、平均响应时间、热门接口等
        return Result.success(
            mapOf(
                "totalCount" to apiLogService.count(),
                "successRate" to "95.5%",
                "avgResponseTime" to "120ms"
            )
        )
    }

    /**
     * 删除接口调用日志
     *
     * @param id 日志ID
     * @return 删除结果
     */
    @ApiLog(apiName = "删除接口日志", description = "根据ID删除接口调用日志")
    @PostMapping("/delete")
    fun delete(@RequestParam id: Long): Result<Boolean> {
        val result = apiLogService.removeById(id)
        return Result.success(result)
    }

    /**
     * 批量删除接口调用日志
     *
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @ApiLog(apiName = "批量删除接口日志", description = "批量删除接口调用日志")
    @PostMapping("/batchDelete")
    fun batchDelete(@RequestBody ids: List<Long>): Result<Boolean> {
        val result = apiLogService.removeByIds(ids)
        return Result.success(result)
    }

    /**
     * 清理历史日志
     *
     * @param days 保留天数
     * @return 清理结果
     */
    @ApiLog(apiName = "清理历史日志", description = "清理指定天数之前的接口调用日志")
    @PostMapping("/clean")
    fun cleanHistory(@RequestParam(defaultValue = "30") days: Int): Result<Int> {
        val cutoffTime = DateTimeUtils.now().minusDays(days.toLong())
        val queryWrapper = QueryWrapper<TApiLog>()
        queryWrapper.lt("create_time", cutoffTime)

        val count = apiLogService.count(queryWrapper).toInt()
        val result = apiLogService.remove(queryWrapper)

        return if (result) Result.success(count) else Result.error("清理失败")
    }
}
