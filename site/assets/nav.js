/* 导航、页面标题与搜索索引（双语）/ Navigation, page titles and search index (bilingual)
   新增页面：在 CC_NAV 里加一项，再往 CC_SEARCH 里补若干条即可。 */

window.CC_VERSION = { bukkit: "4.1.0-bukkit", folia: "4.1.0-folia" };
window.CC_REPO = "Hi-PolarBear/CreeperConfettiPro";

window.CC_NAV = [
  {
    title: { en: "Get started", zh: "开始" },
    items: [
      {
        href: "index.html",
        label: { en: "Overview", zh: "项目概览" },
        title: { en: "Overview · CreeperConfettiPro", zh: "项目概览 · CreeperConfettiPro 文档" }
      },
      {
        href: "install.html",
        label: { en: "Installation", zh: "安装与部署" },
        title: { en: "Installation · CreeperConfettiPro", zh: "安装与部署 · CreeperConfettiPro 文档" }
      },
      {
        href: "download.html",
        label: { en: "Download", zh: "下载" },
        title: { en: "Download · CreeperConfettiPro", zh: "下载 · CreeperConfettiPro 文档" }
      }
    ]
  },
  {
    title: { en: "Configuration", zh: "配置" },
    items: [
      {
        href: "config.html",
        label: { en: "config.yml", zh: "配置文件" },
        title: { en: "Configuration · CreeperConfettiPro", zh: "配置文件 · CreeperConfettiPro 文档" }
      },
      {
        href: "styles.html",
        label: { en: "Styles & chances", zh: "烟花样式与概率" },
        title: { en: "Styles & chances · CreeperConfettiPro", zh: "烟花样式与概率 · CreeperConfettiPro 文档" }
      },
      {
        href: "languages.html",
        label: { en: "Languages", zh: "多语言" },
        title: { en: "Languages · CreeperConfettiPro", zh: "多语言 · CreeperConfettiPro 文档" }
      }
    ]
  },
  {
    title: { en: "Usage", zh: "使用" },
    items: [
      {
        href: "commands.html",
        label: { en: "Commands & permissions", zh: "指令与权限" },
        title: { en: "Commands & permissions · CreeperConfettiPro", zh: "指令与权限 · CreeperConfettiPro 文档" }
      },
      {
        href: "gui.html",
        label: { en: "In-game GUI", zh: "图形界面" },
        title: { en: "In-game GUI · CreeperConfettiPro", zh: "图形界面 · CreeperConfettiPro 文档" }
      }
    ]
  },
  {
    title: { en: "Technical", zh: "技术" },
    items: [
      {
        href: "folia.html",
        label: { en: "Bukkit & Folia builds", zh: "Bukkit 与 Folia 双分支" },
        title: { en: "Bukkit & Folia builds · CreeperConfettiPro", zh: "Bukkit 与 Folia 双分支 · CreeperConfettiPro 文档" }
      },
      {
        href: "build.html",
        label: { en: "Build & extend", zh: "构建与二次开发" },
        title: { en: "Build & extend · CreeperConfettiPro", zh: "构建与二次开发 · CreeperConfettiPro 文档" }
      }
    ]
  },
  {
    title: { en: "More", zh: "其他" },
    items: [
      {
        href: "faq.html",
        label: { en: "FAQ", zh: "常见问题" },
        title: { en: "FAQ · CreeperConfettiPro", zh: "常见问题 · CreeperConfettiPro 文档" }
      }
    ]
  }
];

/* [页面, 锚点, 英文小节, 英文关键词, 中文小节, 中文关键词] */
window.CC_SEARCH = [
  ["index.html", "#what", "What this plugin does", "overview intro creeper confetti explosion", "这是什么插件", "介绍 概述 简介 苦力怕 彩纸"],
  ["index.html", "#flow", "How it works", "flow event EntityExplodeEvent cancel", "工作流程", "原理 流程 事件 取消爆炸"],
  ["index.html", "#branch", "Which build to pick", "bukkit folia branch paper spigot choose", "两个分支怎么选", "分支 区别 选择 服务端"],
  ["index.html", "#features", "Feature list", "features styles gui i18n aliases", "功能一览", "特性 功能 样式 界面 多语言"],

  ["install.html", "#require", "Requirements", "java version server 1.13 1.19.4", "环境要求", "java 版本 要求 服务端 兼容"],
  ["install.html", "#install", "Install steps", "install deploy plugins folder restart", "安装步骤", "安装 部署 放入 重启"],
  ["install.html", "#files", "Generated files", "folder config languages backups", "首次启动会生成什么", "目录 文件 生成"],
  ["install.html", "#verify", "Verify the install", "verify test console log summon", "验证安装", "验证 测试 检查 日志"],
  ["install.html", "#uninstall", "Uninstall & roll back", "uninstall remove rollback backup", "卸载与回滚", "卸载 删除 回滚 备份"],

  ["download.html", "#downloads", "Latest downloads", "download latest release jar bukkit folia github", "下载最新版", "下载 最新 发行版 jar"],
  ["download.html", "#which", "Which build to pick", "choose build bukkit folia java", "该选哪个版本", "选择 版本 服务端"],
  ["download.html", "#how", "After downloading", "install after download plugin folder", "下载之后", "安装 插件目录 重启"],
  ["download.html", "#notes", "Release notes", "changelog release notes version", "发行说明", "更新 日志 版本"],

  ["config.html", "#structure", "Config structure", "config.yml keys structure overview", "配置结构总览", "配置 结构 字段 一览"],
  ["config.html", "#chance", "confetti_chance", "chance probability percent trigger", "confetti_chance", "触发概率 整体 百分比"],
  ["config.html", "#aliases", "command-aliases", "alias cc confetti conflict", "command-aliases", "指令别名 冲突"],
  ["config.html", "#language", "language", "language auto code setting", "language", "语言 设置 auto 代码"],
  ["config.html", "#debug", "debug", "debug log verbose troubleshoot", "debug", "调试 日志 排错"],
  ["config.html", "#migration", "Config version & migration", "config-version migrate backup upgrade", "配置版本与自动迁移", "迁移 升级 备份"],
  ["config.html", "#sample", "Full example", "sample full yml template", "完整示例", "示例 模板 完整"],
  ["config.html", "#rewrite", "Rewritten by commands", "serialized format rewrite", "指令改写后的格式", "序列化 格式 重写"],

  ["styles.html", "#schema", "Style schema", "firework_styles type flicker trail colors fade", "样式结构", "样式 字段 结构"],
  ["styles.html", "#aliases", "Accepted key spellings", "alias key color fade_colors", "兼容写法", "兼容 键名 别名"],
  ["styles.html", "#types", "Firework shapes", "shape type BALL STAR BURST CREEPER configurable", "烟花形状", "形状 类型 可配置"],
  ["styles.html", "#colors", "Color formats", "color hex rgb name", "颜色写法", "颜色 十六进制 RGB 名称"],
  ["styles.html", "#color-name", "Named colors", "named color list RED LIME AQUA", "颜色名称", "颜色 名称 列表"],
  ["styles.html", "#multi", "Multiple effects per style", "multi effects list combine", "一个样式包含多个效果", "多效果 组合"],
  ["styles.html", "#weights", "Weights & auto-compensation", "weight chance 100 normalize", "概率权重与自动补偿", "权重 概率 100 归一化"],
  ["styles.html", "#capture", "Capture a style in game", "add set hand firework capture", "从游戏内采集样式", "手持 烟花 采集 添加"],

  ["languages.html", "#supported", "Supported languages", "language list codes zh en ja", "支持的语言", "语言 列表 代码"],
  ["languages.html", "#legacy", "Legacy language codes", "legacy code zh zht en", "旧版代码", "旧版 代码 转换"],
  ["languages.html", "#auto", "Auto detection (GeoIP)", "auto geoip detect country", "自动检测原理", "自动 检测 地区 国家"],
  ["languages.html", "#mapping", "Country to language mapping", "mapping country code table", "国家代码到语言的映射", "映射 国家 代码"],
  ["languages.html", "#custom", "Custom language files", "custom translate yml placeholders", "自定义语言文件", "自定义 翻译 占位符"],
  ["languages.html", "#colors", "Color codes", "color codes ampersand format", "颜色代码", "颜色代码 格式"],

  ["commands.html", "#overview", "Command overview", "commands list subcommands help", "指令总览", "指令 命令 列表 帮助"],
  ["commands.html", "#effect", "effect subcommand", "effect list add set remove reset preview", "effect 子命令", "样式 管理 增删"],
  ["commands.html", "#chance", "chance subcommand", "chance percent probability", "chance 子命令", "概率 百分比"],
  ["commands.html", "#gui", "gui subcommand", "gui menu interface", "gui 子命令", "界面 菜单"],
  ["commands.html", "#language", "language subcommand", "language lang set reload", "language 子命令", "语言 切换"],
  ["commands.html", "#reload", "reload subcommand", "reload rl config", "reload 子命令", "重载"],
  ["commands.html", "#permissions", "Permission nodes", "permission node op luckperms", "权限节点", "权限 节点 op"],
  ["commands.html", "#tab", "Tab completion", "tab complete suggestion", "Tab 补全", "补全 自动完成"],

  ["gui.html", "#open", "Opening the GUI", "gui open menu command", "打开界面", "界面 打开 菜单"],
  ["gui.html", "#list", "Style list page", "list page slots paging add reset", "样式列表页", "列表 槽位 翻页 添加"],
  ["gui.html", "#edit", "Style edit page", "edit chance plus minus delete preview", "样式编辑页", "编辑 加减 概率 删除"],
  ["gui.html", "#limits", "Edge cases", "limit page delete last style", "界面里的边界情况", "边界 分页 删除"],

  ["folia.html", "#why", "Why two branches", "folia region thread no main thread", "为什么需要两个分支", "分支 原因 线程 区域"],
  ["folia.html", "#diff", "Code differences", "diff compare Schedulers volatile synchronized", "代码差异对照", "差异 对比 源码"],
  ["folia.html", "#schedulers", "Scheduler wrapper", "Schedulers runOnEntity runGlobal runAsync", "调度器封装", "调度 封装 区域"],
  ["folia.html", "#thread", "Thread-safety strategy", "thread safe volatile synchronized cache", "线程安全策略", "线程安全 缓存 锁"],
  ["folia.html", "#checklist", "Porting checklist", "port checklist migrate folia", "迁移检查清单", "迁移 清单 移植"],

  ["build.html", "#build", "Building from source", "maven build package jar compile", "从源码构建", "构建 编译 打包"],
  ["build.html", "#deps", "Dependencies & shading", "dependency shade bstats relocation", "依赖与打包方式", "依赖 shade 重定位"],
  ["build.html", "#structure", "Project structure", "structure packages classes tree", "项目结构", "结构 目录 包"],
  ["build.html", "#classes", "Key classes", "classes manager parser migrator", "关键类说明", "类 说明 职责"],
  ["build.html", "#extend", "Extension points", "extend modify add command keep damage world filter", "扩展点", "扩展 二次开发 修改"],

  ["faq.html", "#damage", "No damage at all", "no damage no block break cancelled", "爆炸完全没有伤害", "伤害 没有 破坏"],
  ["faq.html", "#no-firework", "No firework appeared", "no firework nothing happens chance", "点燃了但没有烟花", "没有 烟花 不触发"],
  ["faq.html", "#too-small", "Firework looks small", "small power ground level BALL_LARGE", "烟花很小", "小 高度 贴地"],
  ["faq.html", "#alias", "/cc is unknown", "unknown command alias conflict", "cc 指令无效", "指令 无效 冲突 未知"],
  ["faq.html", "#percent", "Chances do not add to 100", "percent 100 sum normalize", "概率加起来不是 100", "概率 合计 归一化"],
  ["faq.html", "#style-missing", "Style missing from the list", "style missing invalid color parse", "样式没出现在列表里", "样式 缺失 解析"],
  ["faq.html", "#upgrade", "Config changed after upgrade", "upgrade migrate backup config", "升级后配置变了", "升级 迁移 备份"],
  ["faq.html", "#lang", "Language stays English", "language english geoip failed offline", "语言一直是英文", "语言 英文 失败 离线"],
  ["faq.html", "#encoding", "Console shows garbled text", "encoding utf-8 chcp console", "控制台中文乱码", "乱码 编码 控制台"],
  ["faq.html", "#performance", "Performance impact", "performance lag many creepers", "性能影响", "性能 卡顿"],
  ["faq.html", "#bstats", "Does it report data", "bstats metrics privacy", "插件会上报数据吗", "统计 隐私 数据"],
  ["faq.html", "#folia-error", "Folia fails to start", "folia error java 17 startup", "Folia 上启动报错", "报错 启动 版本"]
];
