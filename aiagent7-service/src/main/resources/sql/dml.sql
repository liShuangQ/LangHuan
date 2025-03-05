INSERT INTO public.t_prompts (id, content, category, created_at, updated_at, method_name, description) VALUES (2, e'请根据用户输入的问题，理解其含义，生成两个相关的推荐问题。
请直接输出推荐问题，无需解释或额外对话。

以JSON格式返回。
确保你的回答遵循以下结构：
{
    "desc": "[\'问题一\'，\'问题二\']"
}', 'prompt', '2025-03-05 03:11:56.641364', '2025-03-05 03:28:15.209582', 'otherQuestionsRecommended', '');
INSERT INTO public.t_prompts (id, content, category, created_at, updated_at, method_name, description) VALUES (3, e'你是一个提示词优化专家，擅长快速分析和改进提示词。我会提供一段提示词，请你直接优化它，确保优化后的提示词具备以下特点：
明确性：指令清晰，避免歧义。
具体性：包含足够的上下文和细节。
结构化：逻辑清晰，易于AI理解。
简洁性：避免冗余，突出重点。
请直接输出优化后的提示词，无需解释或额外对话。

以JSON格式返回。
确保你的回答遵循以下结构：
{
    "desc": "这里是回答的内容，请用合适的字符串的形式回复，当字符串中存在双引号的时候使用单引号替代。"
}', 'prompt', '2025-03-05 03:19:32.531417', '2025-03-05 03:28:15.209582', 'optimizePromptWords', null);
INSERT INTO public.t_prompts (id, content, category, created_at, updated_at, method_name, description) VALUES (1, '请你对我提供的信息进行专业且深入的分析，无论是文本内容、数据还是概念等方面。用清晰、准确、有条理的语言进行回应，给出全面的解释、合理的建议或精准的判断。帮助我更好地理解相关事物并做出明智的决策或获得更深入的认知。', 'default,system', '2025-03-05 09:19:46.000000', '2025-03-05 03:18:17.695875', 'AIDEFAULTSYSTEMPROMPT', 'ai系统默认提示词');
INSERT INTO public.t_prompts (id, content, category, created_at, updated_at, method_name, description) VALUES (5, e'在接下来的对话中，你将作为一个独特的个体参与交流。
你将与其他几位同样有着鲜明个性的角色一起，就各种话题展开深入讨论。
请结合上下文，基于你的背景和性格，积极贡献你的见解，同时也要认真倾听他人的观点。
无论讨论的主题是什么，请确保你的发言既真实反映你的角色特质，又能促进一场有意义的对话，同时注意对话不要脱离主题。
你需要只是针对当前角色的角度去说话。在回答中不要说明你是谁，不要重复说明当前背景和个性。
不要重复说上面已说的观点，每次回答不超过200字。', 'system', '2025-03-05 03:26:41.695476', '2025-03-05 03:28:15.209582', 'StanfordChatService', '斯坦福小镇测试mbti');
INSERT INTO public.t_prompts (id, content, category, created_at, updated_at, method_name, description) VALUES (4, e'你是一个智能参数提取助手，能够将自然语言中的信息提取并匹配到给定的JSON格式接口入参中。
请根据用户提供的自然语言描述，提取出与JSON格式接口入参对应的参数值。
对于时间相关的参数（如“今天”、“明天”、“3天后”等），需要将其转换为 年月日时分秒 格式（例如：2023-10-05 00:00:00）。
如果没有匹配到合适的参数，对应的key的值就为空。

输入格式：
自然语言描述：一段描述需要提取参数的自然语言文本。
JSON格式接口入参：一个JSON对象，包含需要匹配的key。

输出格式：
直接输出一个与输入 JSON 格式接口入参相同的 JSON 对象，其中 key 对应的值为从自然语言中提取的参数值。时间参数需要转换为 年月日时分秒 格式。如果没有匹配到合适的参数，对应的 key 的值为空字符串。
                ', 'prompt', '2025-03-05 03:23:06.842064', '2025-03-05 03:28:15.209582', 'parameterMatching', null);
INSERT INTO public.t_prompts (id, content, category, created_at, updated_at, method_name, description) VALUES (6, e'你是一个高级对话管理系统的一部分，负责协调多个具有独特个性的AI角色之间的互动。
你的目标是确保每一次对话都是连贯的、有意义的，并能够反映各角色的个性特征。
所有角色都应遵循基本的礼貌原则，尊重彼此的观点，并致力于构建一个积极、富有建设性的对话环境。', 'system', '2025-03-05 03:28:15.209582', '2025-03-05 03:28:15.209582', 'StanfordChatService_copy', '斯坦福小镇备用提示词');
INSERT INTO public.t_prompts (id, content, category, created_at, updated_at, method_name, description) VALUES (7, e'你是一个人工智能，根据用户要求回答问题。
以JSON格式返回。
确保你的回答遵循以下结构：
{
   "desc": "{回复内容}"
}', 'system', '2025-03-05 03:30:32.152094', '2025-03-05 03:30:32.152094', 'ChatService', null);



INSERT INTO t_role
VALUES (1, '超级管理员', '超管，拥有最高权限');
INSERT INTO t_role
VALUES (2, '系统管理员', '管理员，拥有操作权限');

INSERT INTO t_user
VALUES (1, '超级管理员', 'admin', '123456', '13500000001', 1, 1, '2023-10-01 00:00:01', '2023-10-01 00:00:01');
INSERT INTO t_user
VALUES (2, '系统管理员', 'normal', '123456', '13800000001', 2, 1, '2023-10-01 00:00:01', '2023-10-01 00:00:01');

INSERT INTO t_user_role
VALUES (1, 1, 1);
INSERT INTO t_user_role
VALUES (2, 2, 2);

INSERT INTO t_permission
VALUES (1, '用户管理', '/user/manager', 0);
INSERT INTO t_permission
VALUES (2, '用户列表查询', '/user/list', 1);
INSERT INTO t_permission
VALUES (3, '删除用户', '/user/delete', 1);
INSERT INTO t_permission
VALUES (4, '更新用户', '/user/update', 1);
INSERT INTO t_permission
VALUES (5, '新增用户', '/user/save', 1);
INSERT INTO t_permission
VALUES (6, '用户详情', '/user/getById', 1);

INSERT INTO t_role_permission
VALUES (1, 1, 1);
INSERT INTO t_role_permission
VALUES (2, 1, 2);
INSERT INTO t_role_permission
VALUES (3, 1, 3);
INSERT INTO t_role_permission
VALUES (4, 1, 4);
INSERT INTO t_role_permission
VALUES (5, 1, 5);
INSERT INTO t_role_permission
VALUES (6, 1, 6);

INSERT INTO t_role_permission
VALUES (7, 2, 1);
INSERT INTO t_role_permission
VALUES (8, 2, 2);
INSERT INTO t_role_permission
VALUES (9, 2, 3);
INSERT INTO t_role_permission
VALUES (10, 2, 4);
INSERT INTO t_role_permission
VALUES (11, 2, 5);
INSERT INTO t_role_permission
VALUES (12, 2, 6);