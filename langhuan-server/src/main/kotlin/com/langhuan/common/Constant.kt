package com.langhuan.common


object Constant {
    // JWT密钥，用于签名和验证token，需要足够长以确保安全性
    const val SECRET = "dGhpcyBpcyBhIGJhc2U2NCBrZXkgd2l0aCBmb3IgZnJvbSBzZWN1cmUgcmluZw=="

    // ADMIN的默认管理员账号的密码（明文）
    const val ADMIN_PASSWORD = "asb#1234"

    // Token过期时间，单位为分钟
    const val EXPIRE = 60 * 24 * 7L

    // HTTP请求头中的token字段名
    const val HEADER = "Authorization"

    // 访问白名单
    val URL_WHITELIST = arrayOf(
        "/favicon.ico", "/user/login", "/user/register", "/user/logout",
        "/service/user/login", "/service/user/register"
    )

    // ai的rag默认导出的分隔符
    const val DEFAULT_RAG_EXPORT_SPLIT = "======"

    // ai的单id最大记忆消息数 必须大于0
    const val MESSAGEWINDOWCHATMEMORYMAX = 10

    // ai系统默认提示词数据库缓存定时刷新时间
    const val AIDEFAULTSYSTEMPROMPTRECTIME = 1000 * 60 * 60 * 2

    // ai系统默认提示词
    const val AINULLDEFAULTSYSTEMPROMPT = """
        你的名字是琅嬛，你是一个智能助手，始终以清晰、准确、简洁的方式回答用户问题。
        你的回答必须使用 Markdown 格式，包括适当的标题、列表、代码块（如需要）和段落结构，避免以 "```" 格式开头出现。
        避免冗余解释，直接提供有用信息。如果问题涉及事实性内容，请确保信息可靠；如果不确定，请明确说明。
    """

    // ai系统默认问答顾问提示词
    const val AIDEFAULTQUESTIONANSWERADVISORRPROMPT = """
        以下是从知识库中检索到的相关上下文（使用 --------------------- 包围，可能为空或不相关）：
        ---------------------
        {question_answer_context}
        ---------------------
        请基于上述上下文回答用户问题。
        如果上下文为空、与问题无关，或未包含足以回答问题的信息，请明确告知用户“未在知识库中找到相关信息”，然后基于你自身的知识直接回答问题。
    """

    // ai系统默认安全顾问 （后期考虑放数据库）
    val AIDEFAULTSAFEGUARDADVISOR = listOf<String>()


    val CATEGORYENUM = listOf(
        mapOf("label" to "默认", "value" to "default"),
        mapOf("label" to "系统", "value" to "system"),
        mapOf("label" to "提示词", "value" to "prompt")
    )

    // 设置相似度阈值，常是一个介于 0 和 1 之间的浮点数，例如 0.5、0.7、0.8
    // 等，如果设置得过高，可能会没有结果返回；如果设置得过低，可能会返回大量不相关的结果
    const val RAGWITHSIMILARITYTHRESHOLD = 0.4

    // 各路召回的个数，例如设置为20，即为20个向量和20个关键字
    const val RAGCALLBACKTOPK = 20

    // 给rerank模型的数量
    const val RAGRERANKTOPK = 20

    // 给模型读的数量
    const val LLM_RAG_TOPN = 5

    // {springai根据向量距离的得分，bm25得分，手工排名}
    val LINEARWEIGHTING = doubleArrayOf(0.6, 0.4, 0.2)

    // 可信结果分数阈值，大于这个分数才喂模型
    const val LINEARWEIGHTINGNUM = 0.5

    // 定义固定的缓存键
    const val CACHE_KEY = "file_id_cache"

    // 个人空间的默认文件组名 （和前端约定好，前端按这个名字查询）
    const val DEFAULTFILEGROUPNAME = "\${user}_知识空间文件组"

    // 个人空间的默认文件名 （和前端约定好，前端按这个名字查询）
    const val DEFAULTFILEGROUPFILE = "\${user}_知识空间"

    // 意图识别的类别
    const val DEFAULTINTENTIONTYPEID = "chat"

    // 可扩展，若未来新增任务，请严格保持「id / task / examples / description」四字段结构。
    data class IntentionItem(
        var id: String? = null,
        var task: String? = null,
        var examples: List<String>? = null,
        var notExamples: List<String>? = null,
        var description: String? = null,
        var attention: String? = null
    )

    val INTENTIONTYPE = mutableListOf<IntentionItem>().apply {
        add(IntentionItem().apply {
            id = "chat"
            task = "聊天"
            description = "无任何特定功能需求的日常闲聊问答等"
            examples = listOf("你好", "回答这个问题", "什么是xxx")
            notExamples = listOf("")
            attention = "当不符合其他任务的时候，默认选择这个"
        })
        add(IntentionItem().apply {
            id = "add_personal_knowledge_space"
            task = "添加个人知识空间"
            description = "用户明确请求添加知识到个人知识存储区域。"
            examples = listOf("xxx添加知识库xxx", "xxx添加到个人文档xxx", "添加到我的空间", "记录知识")
            notExamples = listOf("")
            attention =
                "注意例子（Examples）中的添加或者记录等字样的关键字前后可能含有其他信息（例如：理解xxx的信息，添加知识库xxxx），也可能不带（例如：添加知识库，···）。"
        })
    }
}
