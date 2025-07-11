package com.langhuan.model.mapper;

import com.langhuan.model.domain.TRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lishuangqi
* @description 针对表【t_role(系统角色表)】的数据库操作Mapper
* @createDate 2024-12-24 10:01:31
* @Entity domain.model.com.langhuan.TRole
*/

@Mapper
public interface TRoleMapper extends BaseMapper<TRole> {

}




