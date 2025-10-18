package com.langhuan.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.model.domain.TFileUrl
import com.langhuan.model.mapper.TFileUrlMapper
import org.springframework.stereotype.Service

/**
* @author 20546
* @description 针对表【t_file_url】的数据库操作Service实现
* @createDate 2025-07-29 15:17:19
*/
@Service
class TFileUrlService : ServiceImpl<TFileUrlMapper, TFileUrl>()
