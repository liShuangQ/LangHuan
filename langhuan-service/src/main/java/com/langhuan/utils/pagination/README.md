# JdbcTemplate分页工具使用说明

## 概述

本工具实现了与MyBatis Plus分页插件完全相同的数据输出格式，使用JdbcTemplate进行数据库查询，专门针对PostgreSQL数据库优化。前端无需任何改动即可使用。

## 核心组件

### 1. JdbcPage<T> 
- 实现了MyBatis Plus的`IPage<T>`接口
- 提供与MyBatis Plus相同的分页数据结构
- 支持链式调用和完整的分页信息

### 2. JdbcPaginationHelper
- 核心分页查询工具类
- 提供多种分页查询方法
- 自动生成COUNT查询SQL
- 支持PostgreSQL的LIMIT/OFFSET语法

### 3. QueryCondition
- 动态查询条件构建器
- 支持常见的查询条件（等于、模糊查询、范围查询等）
- 防止SQL注入

## 返回数据格式

```json
{
  "current": 1,          // 当前页码
  "size": 10,            // 每页大小
  "total": 100,          // 总记录数
  "pages": 10,           // 总页数
  "records": [...],      // 数据列表
  "hasPrevious": false,  // 是否有上一页
  "hasNext": true        // 是否有下一页
}
```
## 自动驼峰命名转换

工具会自动将数据库字段的下划线命名转换为驼峰命名，例如：

| 数据库字段 | 返回的JSON键名 |
| ---------- | -------------- |
| user_name  | userName       |
| created_at | createdAt      |
| api_log_id | apiLogId       |
| is_active  | isActive       |

**注意**：此功能仅在 `selectPageForMap` 方法中生效，使用自定义RowMapper时需手动处理。

## 使用方法

### 1. 基本用法

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final JdbcPaginationHelper paginationHelper;
    
    public IPage<Map<String, Object>> getUserList(int pageNum, int pageSize) {
        String sql = "SELECT * FROM t_user ORDER BY id DESC";
        return paginationHelper.selectPageForMap(sql, new Object[]{}, pageNum, pageSize);
    }
}
```

### 2. 使用自定义RowMapper

```java
public IPage<TUser> getUserPageList(String name, int pageNum, int pageSize) {
    // 构建查询条件
    JdbcPaginationHelper.QueryCondition condition = new JdbcPaginationHelper.QueryCondition()
            .like("name", name);
    
    // 构建SQL
    String sql = "SELECT * FROM t_user" + condition.getWhereClause() + " ORDER BY id DESC";
    
    // 执行查询
    return paginationHelper.selectPage(sql, condition.getParams(), new UserRowMapper(), pageNum, pageSize);
}
```

### 3. 复杂查询示例

```java
public IPage<Map<String, Object>> getComplexData(String keyword, LocalDateTime startTime, int pageNum, int pageSize) {
    // 构建查询条件
    JdbcPaginationHelper.QueryCondition condition = new JdbcPaginationHelper.QueryCondition()
            .like("u.name", keyword)
            .ge("al.create_time", startTime);
    
    // 复杂SQL
    String dataSql = """
        SELECT u.name, u.username, al.api_name, al.create_time
        FROM t_user u
        LEFT JOIN t_api_log al ON u.username = al.username
        """ + condition.getWhereClause() + """
        ORDER BY al.create_time DESC
        """;
    
    // 自定义COUNT SQL（可选）
    String countSql = """
        SELECT COUNT(*)
        FROM t_user u
        LEFT JOIN t_api_log al ON u.username = al.username
        """ + condition.getWhereClause();
    
    return paginationHelper.selectPageForMap(dataSql, countSql, condition.getParams(), pageNum, pageSize);
}
```

### 4. 在Controller中使用

```java
@PostMapping("/users/search")
public Result<IPage<TUser>> searchUsers(
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize) {
    
    IPage<TUser> result = userService.getUserPageList(name, pageNum, pageSize);
    return Result.success(result);
}
```

## 查询条件构建器

### 支持的条件类型

```java
QueryCondition condition = new QueryCondition()
    .eq("status", 1)                    // 等于
    .like("name", "张三")               // 模糊查询
    .ge("create_time", startTime)       // 大于等于
    .le("create_time", endTime)         // 小于等于
    .in("id", Arrays.asList(1,2,3))     // IN查询
    .custom("age > ?", 18);             // 自定义条件
```


### 获取SQL和参数

```java
String whereClause = condition.getWhereClause();  // 获取WHERE子句
Object[] params = condition.getParams();          // 获取参数数组
boolean hasConditions = condition.hasConditions(); // 是否有条件
```

## 性能优化

### 1. 自动COUNT优化
- 工具会自动移除ORDER BY子句生成COUNT查询
- 支持自定义COUNT SQL以进一步优化性能

### 2. PostgreSQL特性支持
- 使用LIMIT/OFFSET进行分页
- 支持PostgreSQL的全文搜索
- 支持复杂的统计查询

### 3. 最佳实践
- 对于大数据量查询，建议提供自定义COUNT SQL
- 合理使用数据库索引
- 避免在WHERE条件中使用函数

## 与MyBatis Plus的对比

| 特性           | MyBatis Plus | JdbcTemplate工具    |
| -------------- | ------------ | ------------------- |
| 返回数据格式   | IPage<T>     | IPage<T> (完全相同) |
| 查询性能       | 中等         | 高                  |
| SQL灵活性      | 受限         | 完全自由            |
| 复杂查询支持   | 一般         | 优秀                |
| PostgreSQL特性 | 有限         | 完全支持            |
| 学习成本       | 中等         | 低                  |

## 注意事项

1. **SQL注入防护**：始终使用参数化查询，避免直接拼接用户输入
2. **表名验证**：动态表名查询时需要验证表名的合法性
3. **性能监控**：对于大数据量查询，建议监控查询性能
4. **事务管理**：在Service层正确使用@Transactional注解


## 总结

本工具完全兼容MyBatis Plus的分页插件输出格式，前端无需任何改动。同时提供了更高的性能和更灵活的SQL支持，特别适合需要复杂查询和PostgreSQL特性的场景。 

## `hasConditions()` 方法详解

### 方法定义
```java
public boolean hasConditions() {
    return whereClause.length() > 0;
}
```

### 作用说明
`hasConditions()` 方法用于检查 `QueryCondition` 对象是否包含任何查询条件。它返回一个布尔值：
- `true`：表示已经添加了至少一个查询条件
- `false`：表示还没有添加任何查询条件

### 使用场景

#### 1. **动态SQL构建**
当需要根据是否有条件来动态构建SQL时：

```java
<code_block_to_apply_changes_from>
```

#### 2. **条件验证**
在执行业务逻辑前验证查询条件的有效性：

```java
public IPage<Map<String, Object>> searchWithValidation(String keyword, LocalDateTime startTime) {
    QueryCondition condition = new QueryCondition()
            .like("name", keyword)
            .ge("create_time", startTime);
    
    // 验证是否至少有一个有效条件
    if (!condition.hasConditions()) {
        throw new BusinessException("至少需要提供一个搜索条件");
    }
    
    String sql = "SELECT * FROM t_user" + condition.getWhereClause() + " ORDER BY id DESC";
    return paginationHelper.selectPageForMap(sql, condition.getParams(), 1, 10);
}
```

#### 3. **性能优化**
避免执行不必要的查询：

```java
public IPage<Map<String, Object>> getApiLogs(String username, String apiName, LocalDateTime startTime) {
    QueryCondition condition = new QueryCondition();
    
    // 只有在参数不为空时才添加条件
    if (StringUtils.hasText(username)) {
        condition.eq("username", username);
    }
    if (StringUtils.hasText(apiName)) {
        condition.like("api_name", apiName);
    }
    if (startTime != null) {
        condition.ge("create_time", startTime);
    }
    
    // 如果没有条件，可以选择返回空结果或抛出异常
    if (!condition.hasConditions()) {
        log.warn("查询API日志时未提供任何过滤条件，可能影响性能");
        // 可以选择返回空结果
        return new JdbcPage<>(1, 10);
    }
    
    String sql = "SELECT * FROM t_api_log" + condition.getWhereClause() + " ORDER BY create_time DESC";
    return paginationHelper.selectPageForMap(sql, condition.getParams(), 1, 10);
}
```

#### 4. **日志记录**
记录查询条件的统计信息：

```java
public IPage<Map<String, Object>> searchWithLogging(String keyword, Integer status) {
    QueryCondition condition = new QueryCondition()
            .like("name", keyword)
            .eq("status", status);
    
    // 记录查询条件信息
    if (condition.hasConditions()) {
        log.info("执行用户搜索，条件数量: {}, 参数: {}", 
                condition.getParams().length, Arrays.toString(condition.getParams()));
    } else {
        log.info("执行用户搜索，无过滤条件");
    }
    
    String sql = "SELECT * FROM t_user" + condition.getWhereClause() + " ORDER BY id DESC";
    return paginationHelper.selectPageForMap(sql, condition.getParams(), 1, 10);
}
```

#### 5. **缓存策略**
根据查询条件决定是否使用缓存：

```java
public IPage<Map<String, Object>> getCachedUserList(String name, Integer status) {
    QueryCondition condition = new QueryCondition()
            .like("name", name)
            .eq("status", status);
    
    // 根据是否有条件决定缓存策略
    String cacheKey;
    if (condition.hasConditions()) {
        // 有条件时，使用条件相关的缓存键
        cacheKey = "user_list_filtered_" + Arrays.hashCode(condition.getParams());
    } else {
        // 无条件时，使用通用缓存键
        cacheKey = "user_list_all";
    }
    
    // 检查缓存
    IPage<Map<String, Object>> cached = cacheService.get(cacheKey);
    if (cached != null) {
        return cached;
    }
    
    // 执行查询并缓存结果
    String sql = "SELECT * FROM t_user" + condition.getWhereClause() + " ORDER BY id DESC";
    IPage<Map<String, Object>> result = paginationHelper.selectPageForMap(sql, condition.getParams(), 1, 10);
    cacheService.put(cacheKey, result);
    
    return result;
}
```

### 总结

`hasConditions()` 方法主要用于：

1. **动态SQL构建** - 根据是否有条件来决定SQL结构
2. **条件验证** - 确保查询至少包含一个有效条件
3. **性能优化** - 避免执行无意义的查询
4. **日志记录** - 记录查询条件的统计信息
5. **缓存策略** - 根据条件决定缓存策略

这个方法是 `QueryCondition` 工具类中非常实用的辅助方法，能够帮助开发者更好地控制查询逻辑和优化性能。 