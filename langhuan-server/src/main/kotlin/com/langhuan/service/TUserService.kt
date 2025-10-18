package com.langhuan.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.common.BusinessException
import com.langhuan.common.Constant
import com.langhuan.dao.TUserDao
import com.langhuan.model.domain.TPermission
import com.langhuan.model.domain.TRolePermission
import com.langhuan.model.domain.TUser
import com.langhuan.model.domain.TUserRole
import com.langhuan.model.dto.UserLoginDTO
import com.langhuan.model.mapper.TUserMapper
import com.langhuan.utils.other.JwtUtil
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * @author lishuangqi
 * @description 针对表【t_user(用户表)】的数据库操作Service实现
 * @createDate 2024-12-24 10:01:31
 */
@Service
class TUserService(
    // 用户角色服务，用于处理用户和角色之间的关系
    private val TUserRoleService: TUserRoleService,
    // 角色权限服务，用于处理角色和权限之间的关系
    private val TRolePermissionService: TRolePermissionService,
    // 权限服务，用于执行权限相关的数据库操作
    private val TPermissionService: TPermissionService,
    private val jwtUtil: JwtUtil,
    private val userDao: TUserDao,
    private val cacheService: CacheService
) : ServiceImpl<TUserMapper, TUser>() {

    companion object {
        private val log = LoggerFactory.getLogger(TUserService::class.java)
    }

    /**
     * 根据用户名获取权限列表
     * 首先通过用户名查询用户信息，然后调用getPermissionByUser方法获取权限列表
     */
    @Cacheable(value = ["permission"], key = "#username", sync = true)
    // 这里注意缓存配置，在操作用户后要清空缓存
    fun getPermissionByUsername(username: String): List<TPermission> {
        TUserService.log.info("getPermissionByUsername: {}", username)
        val user = super.getOne(QueryWrapper<TUser>().eq("username", username), true)
        return this.getPermissionByUser(user)
    }

    /**
     * 根据用户ID获取权限列表
     * 首先通过用户ID查询用户信息，然后调用getPermissionByUser方法获取权限列表
     */
    fun getPermissionByUserId(userId: Int): List<TPermission> {
        val user = super.getById(userId)
        return this.getPermissionByUser(user)
    }

    /**
     * 根据用户对象获取权限列表
     * 此方法首先检查用户是否不为空，然后通过用户角色关系获取角色ID列表，
     * 再通过角色权限关系获取权限ID列表，最后根据权限ID列表查询权限信息
     */
    fun getPermissionByUser(user: TUser?): List<TPermission> {
        val permissions = mutableListOf<TPermission>()
        if (user != null) {
            // 获取用户的角色列表
            val userRoles = TUserRoleService.list(
                QueryWrapper<TUserRole>().eq("user_id", user.id)
            )
            if (CollectionUtils.isNotEmpty(userRoles)) {
                // 提取角色ID列表
                val roleIds = userRoles.map { it.roleId }
                // 获取角色对应的权限列表
                val rolePermissions = TRolePermissionService.list(
                    QueryWrapper<TRolePermission>().`in`("role_id", roleIds)
                )
                if (CollectionUtils.isNotEmpty(rolePermissions)) {
                    // 提取权限ID列表
                    val permissionIds = rolePermissions.map { it.permissionId }
                    // 根据权限ID列表查询权限信息
                    val permissionList = TPermissionService.list(
                        QueryWrapper<TPermission>().`in`("id", permissionIds)
                    )
                    permissions.addAll(permissionList)
                }
            }
        }
        return permissions
    }

    fun register(user: TUser): TUser {
        val user1 = super.getOne(QueryWrapper<TUser>().eq("username", user.username))
        if (user1 != null) {
            throw BusinessException("用户名已存在")
        }
        user.password = BCryptPasswordEncoder().encode(user.password) // 密码加密
        user.creationTime = Date()
        super.save(user)
        user.password = null
        return user
    }

    fun change(user: TUser): TUser {
        user.password = BCryptPasswordEncoder().encode(user.password)
        super.update(user, UpdateWrapper<TUser>().eq("username", user.username))
        cacheService.clearPermissionCache(user.username as String)
        return user
    }

    fun login(userLoginDTO: UserLoginDTO, response: HttpServletResponse): Map<String, String> {
        val username = userLoginDTO.username
        val password = userLoginDTO.password
        val user = super.getOne(QueryWrapper<TUser>().eq("username", username))
        if (user == null) {
            throw BusinessException("用户名不存在")
        }
        // HACK: 针对admin单独处理
        if (user.username == "admin") {
            if (password != Constant.ADMIN_PASSWORD) {
                throw BusinessException("用户名或密码错误")
            }
        } else {
            if (!BCryptPasswordEncoder().matches(password, user.password)) {
                throw BusinessException("用户名或密码错误")
            }
        }

        val token = jwtUtil.generateToken(username)
        response.setHeader(JwtUtil.HEADER, token)
        response.setHeader("Access-control-Expost-Headers", JwtUtil.HEADER)
        val map = mutableMapOf<String, String>()
        map["token"] = token

        TUserService.log.info("用户 {} 登录成功,更新时间", username)
        super.update(UpdateWrapper<TUser>().set("last_login_time", Date()).eq("username", username))
        return map
    }

    fun getUserInfoByToken(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val auth = SecurityContextHolder.getContext().getAuthentication()
        val user = super.getOne(QueryWrapper<TUser?>().eq("username", auth.getName()))
        user.password = null
        map.put("user", user)
        map.put("permission", auth.getAuthorities())
        map.put("role", userDao.getUserInfoById(user.id!!))
        return map
    }

    // 数据库事务 出现异常回滚
    @Transactional(rollbackFor = [Exception::class])
    fun delete(userId: Int): Boolean {
        val user = super.getById(userId)
        if (user == null) {
            throw BusinessException("用户不存在")
        }
        if (user.id == 1) {
            throw BusinessException("超级管理员不能删除")
        }
        TUserRoleService.remove(QueryWrapper<TUserRole>().eq("user_id", userId))
        cacheService.clearPermissionCache()
        return super.removeById(userId)
    }

    fun getUserPageList(
        name: String,
        username: String,
        gender: Int?,
        enabled: Int?,
        pageNum: Int,
        pageSize: Int
    ): Page<TUser> {
        val userPage = super.page(
            Page(pageNum.toLong(), pageSize.toLong()),
            QueryWrapper<TUser>()
                .like(!name.isEmpty(), "name", name)
                .like(!username.isEmpty(), "username", username)
                .eq(gender != null, "gender", gender)
                .eq(enabled != null, "gender", enabled)
        )
        for (record in userPage.records) {
            record.password = null
        }
        return userPage
    }

    fun getUserRoles(userId: Int?): List<Map<String, Any>> {
        return userDao.getUserRolesById(userId)
    }

    fun getUserRoles(userName: String): List<Map<String, Any>> {
        return userDao.getUserRolesByName(userName)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun relevancyRoles(userId: Int, roleIds: List<Int>) {
        if (userId == 1 && !roleIds.contains(1)) {
            throw BusinessException("超级管理员不能修改")
        }
        TUserRoleService.remove(QueryWrapper<TUserRole>().eq("user_id", userId))
        val userRoles = roleIds.map { roleId ->
            TUserRole().apply {
                this.userId = userId
                this.roleId = roleId
            }
        }
        TUserRoleService.saveBatch(userRoles)
        cacheService.clearPermissionCache()
    }

    fun getUserRoleListById(roleId: Int): List<Map<String, Any>> {
        return userDao.getUserRoleListById(roleId)
    }
}
