INSERT INTO t_user
VALUES (1, '超级管理员', 'admin', '123456', '12300000001', 1, 1, '2023-10-01 00:00:01', '2023-10-01 00:00:01');


INSERT INTO t_role
VALUES (1, '超级管理员', '超管，拥有最高权限');


INSERT INTO t_user_role
VALUES (1, 1, 1);

-- 用户管理权限
INSERT INTO t_permission
VALUES (1, '用户管理', '/user/manager', 0); -- 新增权限，格式为(id,权限中文,父级别id)
INSERT INTO t_permission
VALUES (2, '用户注册', '/user/register', 1); -- 新增用户权限
INSERT INTO t_permission
VALUES (3, '用户信息修改', '/user/change', 1); -- 修改用户权限
INSERT INTO t_permission
VALUES (4, '查看用户信息', '/user/info/view', 1); -- 查看用户信息权限
INSERT INTO t_permission
VALUES (5, '查看用户列表', '/user/list', 1); -- 查看用户列表权限
INSERT INTO t_permission
VALUES (6, '删除用户', '/user/delete', 1); -- 删除用户权限
INSERT INTO t_permission
VALUES (7, '查看用户角色', '/user/roles/view', 1); -- 查看用户角色权限
INSERT INTO t_permission
VALUES (8, '编辑用户角色', '/user/roles/edit', 1); -- 编辑用户角色权限

INSERT INTO t_role_permission VALUES (1, 1, 1); -- 给管理员角色添加权限，格式为(id,角色id(固定为1),权限id(从上面的sql中获取))
INSERT INTO t_role_permission VALUES (2, 1, 2);
INSERT INTO t_role_permission VALUES (3, 1, 3);
INSERT INTO t_role_permission VALUES (4, 1, 4);
INSERT INTO t_role_permission VALUES (5, 1, 5);
INSERT INTO t_role_permission VALUES (6, 1, 6);
INSERT INTO t_role_permission VALUES (7, 1, 7);
INSERT INTO t_role_permission VALUES (8, 1, 8);

-- 角色管理权限
INSERT INTO t_permission VALUES (9, '角色管理', '/role/manager', 0); -- 父权限：角色管理
INSERT INTO t_permission VALUES (10, '新增角色', '/role/add', 9); -- 新增角色权限
INSERT INTO t_permission VALUES (11, '删除角色', '/role/delete', 9); -- 删除角色权限
INSERT INTO t_permission VALUES (12, '修改角色', '/role/change', 9); -- 修改角色权限
INSERT INTO t_permission VALUES (13, '查看角色列表', '/role/list', 9); -- 查看角色列表权限
INSERT INTO t_permission VALUES (14, '查看角色权限', '/role/permission/view', 9); -- 查看角色权限
INSERT INTO t_permission VALUES (15, '编辑角色权限', '/role/permission/edit', 9); -- 编辑角色权限

INSERT INTO t_role_permission VALUES (9, 1, 9); -- 超级管理员 授予 角色管理权限
INSERT INTO t_role_permission VALUES (10, 1, 10); -- 超级管理员 授予 新增角色权限
INSERT INTO t_role_permission VALUES (11, 1, 11); -- 超级管理员 授予 删除角色权限
INSERT INTO t_role_permission VALUES (12, 1, 12); -- 超级管理员 授予 修改角色权限
INSERT INTO t_role_permission VALUES (13, 1, 13); -- 超级管理员 授予 查看角色列表权限
INSERT INTO t_role_permission VALUES (14, 1, 14); -- 超级管理员 授予 查看角色权限
INSERT INTO t_role_permission VALUES (15, 1, 15); -- 超级管理员 授予 编辑角色权限




-- 权限管理权限
INSERT INTO t_permission VALUES (16, '权限管理', '/permission/manager', 0); -- 父权限：权限管理
INSERT INTO t_permission VALUES (17, '新增权限', '/permission/add', 16); -- 新增权限权限
INSERT INTO t_permission VALUES (18, '删除权限', '/permission/delete', 16); -- 删除权限权限
INSERT INTO t_permission VALUES (19, '修改权限', '/permission/change', 16); -- 修改权限权限
INSERT INTO t_permission VALUES (20, '查看权限列表', '/permission/list', 16); -- 查看权限列表权限

INSERT INTO t_role_permission VALUES (16, 1, 16); -- 超级管理员 授予 权限管理权限
INSERT INTO t_role_permission VALUES (17, 1, 17); -- 超级管理员 授予 新增权限权限
INSERT INTO t_role_permission VALUES (18, 1, 18); -- 超级管理员 授予 删除权限权限
INSERT INTO t_role_permission VALUES (19, 1, 19); -- 超级管理员 授予 修改权限权限
INSERT INTO t_role_permission VALUES (20, 1, 20); -- 超级管理员 授予 查看权限列表权限



-- RAG文件管理权限
INSERT INTO t_permission VALUES (21, 'RAG文件管理', '/rag/file/manager', 0); -- 父权限：RAG文件管理
INSERT INTO t_permission VALUES (22, '删除RAG文件', '/rag/file/delete', 21); -- 删除RAG文件权限
INSERT INTO t_permission VALUES (23, '查询RAG文件', '/rag/file/query', 21); -- 查询RAG文件权限
INSERT INTO t_permission VALUES (24, '查询RAG文件文档', '/rag/file/queryDocumentsByFileId', 21); -- 查询RAG文件文档权限
INSERT INTO t_permission VALUES (25, '查询文件组文件', '/rag/file/getFilesByGroupId', 21); -- 查询文件组文件权限

INSERT INTO t_role_permission VALUES (21, 1, 21); -- 超级管理员 授予 RAG文件管理权限
INSERT INTO t_role_permission VALUES (22, 1, 22); -- 超级管理员 授予 删除RAG文件权限
INSERT INTO t_role_permission VALUES (23, 1, 23); -- 超级管理员 授予 查询RAG文件权限
INSERT INTO t_role_permission VALUES (24, 1, 24); -- 超级管理员 授予 查询RAG文件文档权限
INSERT INTO t_role_permission VALUES (25, 1, 25); -- 超级管理员 授予 查询文件组文件权限

-- RAG文件组管理权限
INSERT INTO t_permission VALUES (26, 'RAG文件组管理', '/rag/file-group/manager', 0); -- 父权限：RAG文件组管理
INSERT INTO t_permission VALUES (27, '获取文件组枚举', '/rag/file-group/getEnum', 26); -- 获取文件组枚举权限
INSERT INTO t_permission VALUES (28, '添加文件组', '/rag/file-group/add', 26); -- 添加文件组权限
INSERT INTO t_permission VALUES (29, '删除文件组', '/rag/file-group/delete', 26); -- 删除文件组权限
INSERT INTO t_permission VALUES (30, '更新文件组', '/rag/file-group/update', 26); -- 更新文件组权限
INSERT INTO t_permission VALUES (31, '查询文件组', '/rag/file-group/query', 26); -- 查询文件组权限

INSERT INTO t_role_permission VALUES (26, 1, 26); -- 超级管理员 授予 RAG文件组管理权限
INSERT INTO t_role_permission VALUES (27, 1, 27); -- 超级管理员 授予 获取文件组枚举权限
INSERT INTO t_role_permission VALUES (28, 1, 28); -- 超级管理员 授予 添加文件组权限
INSERT INTO t_role_permission VALUES (29, 1, 29); -- 超级管理员 授予 删除文件组权限
INSERT INTO t_role_permission VALUES (30, 1, 30); -- 超级管理员 授予 更新文件组权限
INSERT INTO t_role_permission VALUES (31, 1, 31); -- 超级管理员 授予 查询文件组权限

-- RAG操作权限
INSERT INTO t_permission VALUES (32, 'RAG操作管理', '/rag/manager', 0); -- 父权限：RAG操作管理
INSERT INTO t_permission VALUES (33, '读取并分割文档', '/rag/readAndSplitDocument', 32); -- 读取并分割文档权限
INSERT INTO t_permission VALUES (34, '写入文档到向量存储', '/rag/writeDocumentsToVectorStore', 32); -- 写入文档到向量存储权限
INSERT INTO t_permission VALUES (35, '删除文件和文档', '/rag/deleteFileAndDocuments', 32); -- 删除文件和文档权限
INSERT INTO t_permission VALUES (36, '修改文件和文档', '/rag/changeFileAndDocuments', 32); -- 修改文件和文档权限
INSERT INTO t_permission VALUES (37, '召回测试', '/rag/recallTesting', 32); -- 召回测试权限
INSERT INTO t_permission VALUES (38, '修改文档排序', '/rag/changeDocumentsRank', 32); -- 修改文档排序权限
INSERT INTO t_permission VALUES (39, '修改文档文本', '/rag/changeDocumentText', 32); -- 修改文档文本权限
INSERT INTO t_permission VALUES (40, '删除文档文本', '/rag/deleteDocumentText', 32); -- 删除文档文本权限

INSERT INTO t_role_permission VALUES (32, 1, 32); -- 超级管理员 授予 RAG操作管理权限
INSERT INTO t_role_permission VALUES (33, 1, 33); -- 超级管理员 授予 读取并分割文档权限
INSERT INTO t_role_permission VALUES (34, 1, 34); -- 超级管理员 授予 写入文档到向量存储权限
INSERT INTO t_role_permission VALUES (35, 1, 35); -- 超级管理员 授予 删除文件和文档权限
INSERT INTO t_role_permission VALUES (36, 1, 36); -- 超级管理员 授予 修改文件和文档权限
INSERT INTO t_role_permission VALUES (37, 1, 37); -- 超级管理员 授予 召回测试权限
INSERT INTO t_role_permission VALUES (38, 1, 38); -- 超级管理员 授予 修改文档排序权限
INSERT INTO t_role_permission VALUES (39, 1, 39); -- 超级管理员 授予 修改文档文本权限
INSERT INTO t_role_permission VALUES (40, 1, 40); -- 超级管理员 授予 删除文档文本权限


-- 提示词管理权限
INSERT INTO t_permission VALUES (41, '提示词管理', '/prompts/manager', 0); -- 父权限：提示词管理
INSERT INTO t_permission VALUES (42, '获取提示词分类', '/prompts/usePrompt/query', 41); -- 获取提示词分类权限
INSERT INTO t_permission VALUES (43, '新增提示词', '/prompts/usePrompt/add', 41); -- 新增提示词权限
INSERT INTO t_permission VALUES (44, '删除提示词', '/prompts/usePrompt/delete', 41); -- 删除提示词权限
INSERT INTO t_permission VALUES (45, '更新提示词', '/prompts/usePrompt/update', 41); -- 更新提示词权限
INSERT INTO t_permission VALUES (46, '刷新提示词', '/prompts/usePrompt/refresh', 41); -- 刷新提示词权限
INSERT INTO t_permission VALUES (47, '查询提示词', '/prompts/usePrompt/query', 41); -- 查询提示词权限

INSERT INTO t_role_permission VALUES (41, 1, 41); -- 超级管理员 授予 提示词管理权限
INSERT INTO t_role_permission VALUES (42, 1, 42); -- 超级管理员 授予 获取提示词分类权限
INSERT INTO t_role_permission VALUES (43, 1, 43); -- 超级管理员 授予 新增提示词权限
INSERT INTO t_role_permission VALUES (44, 1, 44); -- 超级管理员 授予 删除提示词权限
INSERT INTO t_role_permission VALUES (45, 1, 45); -- 超级管理员 授予 更新提示词权限
INSERT INTO t_role_permission VALUES (46, 1, 46); -- 超级管理员 授予 刷新提示词权限
INSERT INTO t_role_permission VALUES (47, 1, 47); -- 超级管理员 授予 查询提示词权限

-- 聊天反馈管理权限
INSERT INTO t_permission VALUES (48, '聊天反馈管理', '/chatFeedback/manager', 0); -- 父权限：聊天反馈管理
INSERT INTO t_permission VALUES (49, '新增反馈', '/chatFeedback/add', 48); -- 新增反馈权限
INSERT INTO t_permission VALUES (50, '删除反馈', '/chatFeedback/delete', 48); -- 删除反馈权限
INSERT INTO t_permission VALUES (51, '搜索反馈', '/chatFeedback/search', 48); -- 搜索反馈权限
INSERT INTO t_permission VALUES (52, '修改文档文本', '/chatFeedback/changeDocumentTextByString', 48); -- 修改文档文本权限
INSERT INTO t_permission VALUES (53, '查询文档', '/chatFeedback/queryDocumentsByIds', 48); -- 查询文档权限
INSERT INTO t_permission VALUES (54, '修改交互状态', '/chatFeedback/changeInteractionToEnd', 48); -- 修改交互状态权限

INSERT INTO t_role_permission VALUES (48, 1, 48); -- 超级管理员 授予 聊天反馈管理权限
INSERT INTO t_role_permission VALUES (49, 1, 49); -- 超级管理员 授予 新增反馈权限
INSERT INTO t_role_permission VALUES (50, 1, 50); -- 超级管理员 授予 删除反馈权限
INSERT INTO t_role_permission VALUES (51, 1, 51); -- 超级管理员 授予 搜索反馈权限
INSERT INTO t_role_permission VALUES (52, 1, 52); -- 超级管理员 授予 修改文档文本权限
INSERT INTO t_role_permission VALUES (53, 1, 53); -- 超级管理员 授予 查询文档权限
INSERT INTO t_role_permission VALUES (54, 1, 54); -- 超级管理员 授予 修改交互状态权限