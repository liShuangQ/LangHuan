package com.langhuan.service

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.model.domain.VectorStoreRag
import com.langhuan.model.mapper.VectorStoreRagMapper
import org.springframework.stereotype.Service

/**
* @author lishuangqi
* @description 针对表【vector_store_rag】的数据库操作Service实现
* @createDate 2025-01-17 15:03:03
*/
@Service
class VectorStoreService : ServiceImpl<VectorStoreRagMapper, VectorStoreRag>()
