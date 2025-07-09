package com.langhuan.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TApiLog;
import com.langhuan.model.mapper.TApiLogMapper;
import lombok.extern.slf4j.Slf4j;
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