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

    // ai系统默认提示词数据库定时刷新时间
    const val AIDEFAULTSYSTEMPROMPTRECTIME = 1000 * 60 * 60 * 2

    // ai系统默认提示词
    const val AINULLDEFAULTSYSTEMPROMPT = "你是一个人工智能，请根据要求回答用户的问题。"

    // ai系统默认用户级别提示词
    const val AINULLDEFAULTUSERPROMPT = "{user_prompt}"

    // ai系统默认安全顾问 （后期考虑放数据库）
    val AIDEFAULTSAFEGUARDADVISOR = listOf<String>()

    // ai系统默认问答顾问提示词
    const val AIDEFAULTQUESTIONANSWERADVISORRPROMPT = """
            上下文信息rag，在下面用 --------------------- 包围。
            鉴于上下文信息rag和提供的历史信息而非先验知识，回复用户，并且不要体现这个信息是在上下文信息rag中拿到的。
            如果问题和上下文信息rag无关或者答案不在上下文信息rag中，则你自己回答这个问题。并且在回答中不要提示没找到信息，无需解释。
            ---------------------
            {question_answer_context}
            ---------------------
            """
    val CATEGORYENUM = listOf(
        mapOf("label" to "默认", "value" to "default"),
        mapOf("label" to "系统", "value" to "system"),
        mapOf("label" to "提示词", "value" to "prompt")
    )

    // 设置相似度阈值，常是一个介于 0 和 1 之间的浮点数，例如 0.5、0.7、0.8
    // 等，如果设置得过高，可能会没有结果返回；如果设置得过低，可能会返回大量不相关的结果
    const val RAGWITHSIMILARITYTHRESHOLD = 0.4

    // rag的rank排序方法 linearWeighting
    const val RAGRANKMODULETYPE = "linearWeighting"

    // 从向量库返回的设置返回的得分最高结果的数量（注意当使用linearWeighting的时候，这个数同时也是给rerank的数）
    const val RAGWITHTOPK = 10

    // 经过手工排序筛选后给LLM的RAG topn。 实际输出数量。
    // 给模型RAG的数量，也是召回等的结果，这个数量不能小于RAGRANKTOPK或RAGRERANKTOPN等值
    const val LLM_RAG_TOPN = 5

    // 是否开启rerank，当接口中没传递的时候默认使用这个值
    const val ISRAGRERANK = false

    // --- 如果使用线性加权法（linearWeighting）方式配置如下参数 ---
    // {数据库向量距离，spring ai得分，rerank模型距离，手工排名}
    val LINEARWEIGHTING = doubleArrayOf(0.3, 0.2, 0.2, 0.3)

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
        add(IntentionItem().apply {
            id = "understand"
            task = "理解文件"
            description = "用户需要解析、描述或提取图片或者文档（文件）内容。"
            examples =
                listOf("解释文档内容", "文档里有什么", "理解这张图", "图里有什么", "识别图片文字")
            notExamples = listOf("xxx是什么", "xxx是什么表")
            attention = "注意文字关键字中要提到图片或者文档信息。"
        })
        add(IntentionItem().apply {
            id = "feedback_information_yes"
            task = "满意的积极的反馈信息"
            description = "用户对系统、知识、回答等进行反馈，满意的积极的信息"
            examples = listOf("回答的不错", "很满意", "你越来越好了")
            notExamples = listOf("")
            attention = ""
        })
        add(IntentionItem().apply {
            id = "feedback_information_no"
            task = "消极的不满意的反馈信息"
            description = "用户对系统、知识、回答等进行反馈，不满意的消极的信息"
            examples = listOf("不好的回答", "你说的不对", "没有说到我要的回答", "回答有问题")
            notExamples = listOf("")
            attention = ""
        })
    }
}
