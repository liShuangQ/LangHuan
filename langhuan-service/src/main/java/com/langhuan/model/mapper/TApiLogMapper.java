package com.langhuan.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.langhuan.model.domain.TApiLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口调用日志统计Mapper接口
 *
 * @author system
 */
@Mapper
public interface TApiLogMapper extends BaseMapper<TApiLog> {
} 