package com.shuangqi.aiagent7.model.mapper;

import com.shuangqi.aiagent7.model.domain.TUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
* @author lishuangqi
* @description 针对表【t_user_role(用户-角色关系表)】的数据库操作Mapper
* @createDate 2024-12-24 10:01:31
* @Entity com.shuangqi.aiagent7.model.domain.TUserRole
*/

@Mapper
public interface TUserRoleMapper extends BaseMapper<TUserRole> {

}




