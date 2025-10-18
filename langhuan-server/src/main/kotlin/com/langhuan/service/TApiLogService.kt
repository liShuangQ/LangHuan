package com.langhuan.service

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.dao.TApiLogDao
import com.langhuan.model.domain.TApiLog
import com.langhuan.model.mapper.TApiLogMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

/**
 * 接口调用日志统计服务实现类
 *
 * @author system
 */
@Service
class TApiLogService : ServiceImpl<TApiLogMapper, TApiLog>() {

    companion object {
        private val log = LoggerFactory.getLogger(TApiLogService::class.java)
    }

    @Autowired
    private val tApiLogDao: TApiLogDao? = null

    fun search(apiName: String?, username: String?, startTime: LocalDateTime?, endTime: LocalDateTime?, pageNum: Int, pageSize: Int): IPage<Map<String, Any>> {
        return tApiLogDao!!.searchApiLogs(apiName, username, startTime, endTime, pageNum, pageSize) as IPage<Map<String, Any>>
    }

    fun saveApiLog(apiLog: TApiLog): Boolean {
        return try {
            save(apiLog)
        } catch (e: Exception) {
            TApiLogService.log.error("保存接口调用日志失败: {}", e.message, e)
            false
        }
    }

    @Async
    fun saveApiLogAsync(apiLog: TApiLog) {
        try {
            save(apiLog)
            TApiLogService.log.debug("异步保存接口调用日志成功: {}", apiLog.apiName)
        } catch (e: Exception) {
            TApiLogService.log.error("异步保存接口调用日志失败: {}", e.message, e)
        }
    }
}
