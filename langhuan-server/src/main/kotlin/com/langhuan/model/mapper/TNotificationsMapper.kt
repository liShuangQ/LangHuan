package com.langhuan.model.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.langhuan.model.domain.TNotifications

/**
* @author lishuangqi
* @description 针对表【t_notifications(系统通知表，用于存储发送给用户的各类通知信息)】的数据库操作Mapper
* @createDate 2025-06-05 18:17:25
* @Entity langhuan.TNotifications
*/
interface TNotificationsMapper : BaseMapper<TNotifications>
