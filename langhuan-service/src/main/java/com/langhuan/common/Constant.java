package com.langhuan.common;

import com.langhuan.model.pojo.IntentionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Constant {
    // JWT密钥，用于签名和验证token，需要足够长以确保安全性
    public static final String SECRET = "dGhpcyBpcyBhIGJhc2U2NCBrZXkgd2l0aCBmb3IgZnJvbSBzZWN1cmUgcmluZw==";
    // ADMIN的默认管理员账号的密码（明文）
    public static final String ADMIN_PASSWORD = "asb#1234";
    // Token过期时间，单位为分钟
    public static final long EXPIRE = 60 * 24 * 7;
    // HTTP请求头中的token字段名
    public static final String HEADER = "Authorization";
    // 访问白名单
    public static final String[] URL_WHITELIST = {"/favicon.ico", "/user/login", "/user/register", "/user/logout",
            "/service/user/login", "/service/user/register"};
    // ai的rag默认导出的分隔符
    public static final String DEFAULT_RAG_EXPORT_SPLIT = "======";
    // ai的单id最大记忆消息数 必须大于0
    public static final int MESSAGEWINDOWCHATMEMORYMAX = 10;
    // ai系统默认提示词数据库定时刷新时间
    public static final int AIDEFAULTSYSTEMPROMPTRECTIME = 1000 * 60 * 60 * 2;
    // ai系统默认提示词
    public static final String AINULLDEFAULTSYSTEMPROMPT = "你是一个人工智能，请根据要求回答用户的问题。";
    // ai系统默认用户级别提示词
    public static final String AINULLDEFAULTUSERPROMPT = "{user_prompt}";
    // ai系统默认安全顾问 （后期考虑放数据库）
    public static final List<String> AIDEFAULTSAFEGUARDADVISOR = List.of();
    // ai系统默认问答顾问提示词
    public static final String AIDEFAULTQUESTIONANSWERADVISORRPROMPT = """
            上下文信息rag，在下面用 --------------------- 包围。
            鉴于上下文信息rag和提供的历史信息而非先验知识，回复用户，并且不要体现这个信息是在上下文信息rag中拿到的。
            如果问题和上下文信息rag无关或者答案不在上下文信息rag中，则你自己回答这个问题。并且在回答中不要提示没找到信息，无需解释。
            ---------------------
            {question_answer_context}
            ---------------------
            """;
    public static final List<Map<String, Object>> CATEGORYENUM = List.of(
            Map.of("label", "默认", "value", "default"),
            Map.of("label", "系统", "value", "system"),
            Map.of("label", "提示词", "value", "prompt"));
    // 设置相似度阈值，常是一个介于 0 和 1 之间的浮点数，例如 0.5、0.7、0.8
    // 等，如果设置得过高，可能会没有结果返回；如果设置得过低，可能会返回大量不相关的结果
    public static final double RAGWITHSIMILARITYTHRESHOLD = 0.4;
    // rag的rank排序方法 linearWeighting
    public static final String RAGRANKMODULETYPE = "linearWeighting";
    // 从向量库返回的设置返回的得分最高结果的数量（注意当使用linearWeighting的时候，这个数同时也是给rerank的数）
    public static final int RAGWITHTOPK = 10;
    // 经过手工排序筛选后给LLM的RAG topn。 实际输出数量。
    // 给模型RAG的数量，也是召回等的结果，这个数量不能小于RAGRANKTOPK或RAGRERANKTOPN等值
    public static final int LLM_RAG_TOPN = 5;
    // 是否开启rerank，当接口中没传递的时候默认使用这个值
    public static final Boolean ISRAGRERANK = false;
    // --- 如果使用线性加权法（linearWeighting）方式配置如下参数 ---
    // {数据库向量距离，spring ai得分，rerank模型距离，手工排名}
    public static final double[] LINEARWEIGHTING = {0.3, 0.2, 0.2, 0.3};
    // 定义固定的缓存键
    public static final String CACHE_KEY = "file_id_cache";
    // 个人空间的默认文件组名 （和前端约定好，前端按这个名字查询）
    public static final String DEFAULTFILEGROUPNAME = "${user}_知识空间文件组";
    // 个人空间的默认文件名 （和前端约定好，前端按这个名字查询）
    public static final String DEFAULTFILEGROUPFILE = "${user}_知识空间";
    // 意图识别的类别
    public static final String DEFAULTINTENTIONTYPEID = "chat";
    // 可扩展，若未来新增任务，请严格保持「id / task / examples / description」四字段结构。
    public static final List<IntentionItem> INTENTIONTYPE = new ArrayList<>() {{
        add(new IntentionItem() {{
            setId("chat");
            setTask("聊天");
            setExamples(List.of("你好", "回答这个问题", "什么是xxx"));
            setDescription("无任何特定功能需求的日常闲聊问答等");
        }});
        add(new IntentionItem() {{
            setId("understand_image");
            setTask("理解图片");
            setExamples(List.of("解释这张图", "图里有什么", "识别图片文字"));
            setDescription("用户需要解析、描述或提取图片内容。注意文字要提到是图片信息，当没提到图的时候应该是chat");
        }});
        add(new IntentionItem() {{
            setId("add_personal_knowledge_space");
            setTask("添加个人知识空间");
            setExamples(List.of("添加知识库", "添加到个人文档", "添加到我的空间", "记录知识"));
            setDescription("用户明确请求新建个人知识存储区域");
        }});
        add(new IntentionItem() {{
            setId("add_image_content_to_knowledge_base");
            setTask("将图片内容添加到个人知识库");
            setExamples(List.of("把这张图存到知识库", "图片入库", "保存这张图到我的笔记"));
            setDescription("用户希望将图片解析后的内容写入已有个人知识库，注意要提到了图片信息");
        }});
    }};
}

