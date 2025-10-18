package com.langhuan.model.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.langhuan.model.domain.TRolePermission
import org.apache.ibatis.annotations.Mapper

/**
* @author lishuangqi
* @description 针对表【t_role_permission(角色-权限关联表)】的数据库操作Mapper
* @createDate 2024-12-24 10:01:31
* @Entity domain.model.com.langhuan.TRolePermission
*/
@Mapper
interface TRolePermissionMapper : BaseMapper<TRolePermission>
