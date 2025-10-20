package com.langhuan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.dao.TApiLogDao;
import com.langhuan.model.domain.TApiLog;
import com.langhuan.model.mapper.TApiLogMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 接口调用日志统计服务实现类
 *
 * @author system
 */
@Slf4j
@Service
public class TApiLogService extends ServiceImpl<TApiLogMapper, TApiLog> {

    private final TApiLogDao tApiLogDao;

    public TApiLogService(TApiLogDao tApiLogDao) {
        this.tApiLogDao = tApiLogDao;
    }

    public IPage<Map<String, Object>> search(String apiName, String username, LocalDateTime startTime,
            LocalDateTime endTime, int pageNum, int pageSize) {
        return tApiLogDao.searchApiLogs(apiName, username, startTime, endTime, pageNum, pageSize);
    }

    public boolean saveApiLog(TApiLog apiLog) {
        try {
            return save(apiLog);
        } catch (Exception e) {
            log.error("保存接口调用日志失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Async
    public void saveApiLogAsync(TApiLog apiLog) {
        try {
            save(apiLog);
            log.debug("异步保存接口调用日志成功: {}", apiLog.getApiName());
        } catch (Exception e) {
            log.error("异步保存接口调用日志失败: {}", e.getMessage(), e);
        }
    }
}