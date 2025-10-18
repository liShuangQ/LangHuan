package com.langhuan.model.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.langhuan.model.domain.TPermission
import org.apache.ibatis.annotations.Mapper

/**
 * @author lishuangqi
 * @description 针对表【t_permission(系统权限表)】的数据库操作Mapper
 * @createDate 2024-12-24 10:01:31
 * @Entity generator.domain.TPermission
 */
@Mapper
interface TPermissionMapper : BaseMapper<TPermission>
