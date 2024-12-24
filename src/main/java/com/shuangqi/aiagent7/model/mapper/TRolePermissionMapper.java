package com.shuangqi.aiagent7.model.mapper;

import com.shuangqi.aiagent7.model.domain.TRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
* @author lishuangqi
* @description 针对表【t_role_permission(角色-权限关联表)】的数据库操作Mapper
* @createDate 2024-12-24 10:01:31
* @Entity com.shuangqi.aiagent7.model.domain.TRolePermission
*/

@Mapper
public interface TRolePermissionMapper extends BaseMapper<TRolePermission> {

}




