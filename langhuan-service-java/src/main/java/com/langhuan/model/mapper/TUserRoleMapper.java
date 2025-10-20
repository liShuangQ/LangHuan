package com.langhuan.model.mapper;

import com.langhuan.model.domain.TUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lishuangqi
* @description 针对表【t_user_role(用户-角色关系表)】的数据库操作Mapper
* @createDate 2024-12-24 10:01:31
* @Entity domain.model.com.langhuan.TUserRole
*/

@Mapper
public interface TUserRoleMapper extends BaseMapper<TUserRole> {

}




