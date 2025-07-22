文件组权限管理设计方案

设计目标

为文件组添加私有/公开权限控制，实现以下功能：
- 添加文件组时可选择公开或私有
- 私有文件组默认仅创建者和管理员可访问
- 支持将私有文件组权限共享给其他用户
- 改动最小化，便于后续维护

数据库设计

1. 修改现有表结构

-- 修改 t_rag_file_group 表，添加权限相关字段
ALTER TABLE t_rag_file_group ADD COLUMN visibility VARCHAR(20) DEFAULT 'PUBLIC';
ALTER TABLE t_rag_file_group ADD COLUMN is_admin_only BOOLEAN DEFAULT FALSE;

-- 创建文件组权限共享表
CREATE TABLE t_rag_file_group_permission (
id SERIAL PRIMARY KEY,
file_group_id INTEGER NOT NULL,
username VARCHAR(100) NOT NULL,
permission_type VARCHAR(20) DEFAULT 'READ_WRITE', -- READ_WRITE, READ_ONLY
granted_by VARCHAR(100) NOT NULL,
granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
UNIQUE(file_group_id, username)
);

2. 实体类修改

// TRagFileGroup.java 新增字段
private String visibility = "PUBLIC"; // PUBLIC, PRIVATE
private Boolean isAdminOnly = false;

3. 新建权限实体

// model/domain/TRagFileGroupPermission.java
@TableName(value = "t_rag_file_group_permission")
@Data
public class TRagFileGroupPermission {
@TableId
private Integer id;
private Integer fileGroupId;
private String username;
private String permissionType; // READ_WRITE, READ_ONLY
private String grantedBy;
private Date grantedAt;
}

权限控制设计

1. 权限检查工具类

// utils/PermissionCheckUtil.java
public class PermissionCheckUtil {
public static boolean hasFileGroupAccess(TRagFileGroup fileGroup, String currentUser, boolean isAdmin) {
// 公开文件组，所有人可访问
if ("PUBLIC".equals(fileGroup.getVisibility())) {
return true;
}

          // 管理员始终可访问
          if (isAdmin) {
              return true;
          }

          // 创建者可访问
          if (currentUser.equals(fileGroup.getCreatedBy())) {
              return true;
          }

          // 检查是否有权限共享
          return hasSharedPermission(fileGroup.getId(), currentUser);
      }
}

2. 服务层权限检查

// service/PermissionService.java
@Service
public class PermissionService {
public boolean canAccessFileGroup(Integer fileGroupId, String username) {
// 实现权限检查逻辑
}

      public boolean canModifyFileGroup(Integer fileGroupId, String username) {
          // 实现修改权限检查
      }

      public void sharePermission(Integer fileGroupId, String targetUsername, String permissionType, String grantedBy) {
          // 实现权限共享
      }
}

API端点设计

1. 文件组管理接口增强

// 修改 RagFileGroupController
@PostMapping("/add")
public Result addFileGroup(@Valid @RequestBody TRagFileGroup fileGroup) {
fileGroup.setCreatedBy(currentUser);
fileGroup.setVisibility(fileGroup.getVisibility() != null ? fileGroup.getVisibility() : "PUBLIC");
// 保存逻辑
}

@PostMapping("/query")
public Result queryFileGroups(...) {
// 查询时过滤用户有权限的文件组
return Result.success(ragFileGroupService.queryUserFileGroups(..., currentUser, isAdmin));
}

2. 权限管理接口

// RagFileGroupPermissionController.java
@RestController
@RequestMapping("/rag/file-group/permission")
public class RagFileGroupPermissionController {

      @PostMapping("/share")
      public Result sharePermission(@RequestParam Integer fileGroupId, 
                                  @RequestParam String targetUsername,
                                  @RequestParam String permissionType) {
          // 实现权限共享
      }

      @PostMapping("/revoke")
      public Result revokePermission(@RequestParam Integer fileGroupId, 
                                   @RequestParam String targetUsername) {
          // 撤销权限
      }

      @PostMapping("/list")
      public Result listSharedUsers(@RequestParam Integer fileGroupId) {
          // 列出有权限的用户
      }
}

前端提示词（可直接用于AI代码编辑器）

1. 修改文件组添加界面

在添加文件组的表单中增加以下字段：
- 可见性选择器（单选）：公开/PRIVATE
- 当选择"PRIVATE"时，显示权限共享设置区域

修改代码位置：前端文件组管理页面

2. 文件组列表显示优化

在文件组列表中，为每个文件组显示权限标识：
- 公开文件组显示"公开"标签
- 私有文件组显示"私有"标签，并高亮显示
- 鼠标悬停时显示权限详情

修改代码位置：文件组列表组件

3. 权限共享界面

创建权限共享弹窗组件，包含：
- 用户搜索框（支持模糊搜索）
- 权限类型选择（读写/只读）
- 已共享用户列表，支持撤销权限

修改代码位置：新增权限管理组件

最小化改动清单

后端修改

1. TRagFileGroup.java: 添加visibility和isAdminOnly字段
2. TRagFileGroupController.java: 在查询方法中添加权限过滤
3. 新增: TRagFileGroupPermission.java 实体类
4. 新增: PermissionService.java 权限检查服务
5. 新增: RagFileGroupPermissionController.java 权限管理接口

数据库修改

1. t_rag_file_group表: 添加2个新字段
2. 新增表: t_rag_file_group_permission

前端修改

1. 添加文件组表单: 增加可见性选择
2. 文件组列表: 显示权限标识
3. 新增: 权限共享弹窗

后续扩展提示

性能优化提示

当文件组数量很大时，如何优化权限查询性能？
提示：考虑在PermissionService中添加缓存机制，使用Spring Cache缓存用户的权限列表

批量权限管理提示

如何实现批量设置权限？
提示：在PermissionService中添加batchSharePermission方法，支持一次性给多个用户授权

权限审计提示

如何记录权限变更历史？
提示：新增t_rag_file_group_permission_log表，记录所有权限变更操作
