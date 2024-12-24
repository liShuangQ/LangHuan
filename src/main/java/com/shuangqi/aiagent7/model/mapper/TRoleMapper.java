package com.shuangqi.aiagent7.model.mapper;

import com.shuangqi.aiagent7.model.domain.TRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
* @author lishuangqi
* @description 针对表【t_role(系统角色表)】的数据库操作Mapper
* @createDate 2024-12-24 10:01:31
* @Entity com.shuangqi.aiagent7.model.domain.TRole
*/

@Mapper
public interface TRoleMapper extends BaseMapper<TRole> {

}




