package com.example.domain.model

enum class PlatformCategory(val label: String) {
    CN_ARTICLE("国内图文"),
    CN_VIDEO("国内视频"),
    GLOBAL("海外平台")
}

data class PlatformSpec(
    val id: String,
    val name: String,
    val category: PlatformCategory,
    val supportsMarkdown: Boolean,
    val titleMaxChars: Int,
    val bodyMaxChars: Int?,
    val supportsImages: Boolean,
    val supportsVideo: Boolean,
    val highlightNote: String
)

data class PlatformPreset(
    val id: String,
    val name: String,
    val description: String,
    val platformIds: List<String>
)

object PlatformCatalog {
    val allPlatforms: List<PlatformSpec> = listOf(
        // 国内图文
        PlatformSpec(
            id = "wechat_mp",
            name = "微信公众号",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = true,
            titleMaxChars = 64,
            bodyMaxChars = 20000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "桌面端自动转富文本排版，建议配 16:9 封面"
        ),
        PlatformSpec(
            id = "zhihu",
            name = "知乎专栏",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = true,
            titleMaxChars = 100,
            bodyMaxChars = null,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "原生支持 Markdown 导入与代码块高亮"
        ),
        PlatformSpec(
            id = "xiaohongshu",
            name = "小红书",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = false,
            titleMaxChars = 20,
            bodyMaxChars = 1000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "标题限 20 字内，正文建议 1000 字内配竖版图"
        ),
        PlatformSpec(
            id = "toutiao",
            name = "今日头条",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = false,
            titleMaxChars = 30,
            bodyMaxChars = 30000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "标题建议 5-30 字，支持多图自动抓取"
        ),
        PlatformSpec(
            id = "baijiahao",
            name = "百家号",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = false,
            titleMaxChars = 48,
            bodyMaxChars = null,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "适合资讯与深度长文同步"
        ),
        PlatformSpec(
            id = "juejin",
            name = "稀土掘金",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = true,
            titleMaxChars = 80,
            bodyMaxChars = null,
            supportsImages = true,
            supportsVideo = false,
            highlightNote = "原生 Markdown 引擎，技术文章首选"
        ),
        PlatformSpec(
            id = "csdn",
            name = "CSDN",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = true,
            titleMaxChars = 100,
            bodyMaxChars = null,
            supportsImages = true,
            supportsVideo = false,
            highlightNote = "Markdown 无损迁移，支持标签自动映射"
        ),
        PlatformSpec(
            id = "weibo",
            name = "微博头条",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = false,
            titleMaxChars = 32,
            bodyMaxChars = 10000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "支持短动态与头条长文章双模式"
        ),
        PlatformSpec(
            id = "bilibili_read",
            name = "B站专栏",
            category = PlatformCategory.CN_ARTICLE,
            supportsMarkdown = true,
            titleMaxChars = 80,
            bodyMaxChars = 20000,
            supportsImages = true,
            supportsVideo = false,
            highlightNote = "适合 ACG、科技评测图文同步"
        ),

        // 国内视频
        PlatformSpec(
            id = "bilibili_video",
            name = "Bilibili 视频",
            category = PlatformCategory.CN_VIDEO,
            supportsMarkdown = false,
            titleMaxChars = 80,
            bodyMaxChars = 2000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "需在桌面端 Playwright 引擎上传大体积视频"
        ),
        PlatformSpec(
            id = "douyin",
            name = "抖音创作者",
            category = PlatformCategory.CN_VIDEO,
            supportsMarkdown = false,
            titleMaxChars = 55,
            bodyMaxChars = 1000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "支持图文轮播或短视频分镜脚本发布"
        ),
        PlatformSpec(
            id = "kuaishou",
            name = "快手",
            category = PlatformCategory.CN_VIDEO,
            supportsMarkdown = false,
            titleMaxChars = 50,
            bodyMaxChars = 500,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "移动端可先准备文案标签，桌面端执行上传"
        ),
        PlatformSpec(
            id = "wechat_channels",
            name = "微信视频号",
            category = PlatformCategory.CN_VIDEO,
            supportsMarkdown = false,
            titleMaxChars = 60,
            bodyMaxChars = 1000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "通过桌面端扫码维持本地 Cookie 会话"
        ),

        // 海外平台
        PlatformSpec(
            id = "x_twitter",
            name = "X (Twitter)",
            category = PlatformCategory.GLOBAL,
            supportsMarkdown = false,
            titleMaxChars = 280,
            bodyMaxChars = 280,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "标准推文限制 280 字符（桌面端可拆分 Thread）"
        ),
        PlatformSpec(
            id = "medium",
            name = "Medium",
            category = PlatformCategory.GLOBAL,
            supportsMarkdown = true,
            titleMaxChars = 100,
            bodyMaxChars = null,
            supportsImages = true,
            supportsVideo = false,
            highlightNote = "完整保留 Markdown 标题层级与代码块"
        ),
        PlatformSpec(
            id = "devto",
            name = "Dev.to",
            category = PlatformCategory.GLOBAL,
            supportsMarkdown = true,
            titleMaxChars = 128,
            bodyMaxChars = null,
            supportsImages = true,
            supportsVideo = false,
            highlightNote = "支持 Frontmatter 标签与 Canonical URL"
        ),
        PlatformSpec(
            id = "substack",
            name = "Substack",
            category = PlatformCategory.GLOBAL,
            supportsMarkdown = true,
            titleMaxChars = 120,
            bodyMaxChars = null,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "Newsletter 长文一键同步发布"
        ),
        PlatformSpec(
            id = "linkedin",
            name = "LinkedIn",
            category = PlatformCategory.GLOBAL,
            supportsMarkdown = false,
            titleMaxChars = 150,
            bodyMaxChars = 3000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "动态限制 3000 字符，适合专业洞察"
        ),
        PlatformSpec(
            id = "youtube",
            name = "YouTube Studio",
            category = PlatformCategory.GLOBAL,
            supportsMarkdown = false,
            titleMaxChars = 100,
            bodyMaxChars = 5000,
            supportsImages = true,
            supportsVideo = true,
            highlightNote = "移动端编辑标题与简介，桌面端执行视频上传"
        )
    )

    val presets: List<PlatformPreset> = listOf(
        PlatformPreset(
            id = "tech_blog",
            name = "技术博客全发",
            description = "稀土掘金 + CSDN + 知乎 + Dev.to",
            platformIds = listOf("juejin", "csdn", "zhihu", "devto")
        ),
        PlatformPreset(
            id = "cn_media",
            name = "自媒体图文矩阵",
            description = "微信公众号 + 今日头条 + 百家号 + 小红书",
            platformIds = listOf("wechat_mp", "toutiao", "baijiahao", "xiaohongshu")
        ),
        PlatformPreset(
            id = "global_matrix",
            name = "出海英文矩阵",
            description = "X + Medium + Substack + LinkedIn",
            platformIds = listOf("x_twitter", "medium", "substack", "linkedin")
        )
    )

    fun findById(id: String): PlatformSpec? = allPlatforms.find { it.id == id }
}
