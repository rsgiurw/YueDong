# DESIGN.md — 悦动视觉世界

> 由 impeccable documenter 从已构建的界面记录（ground truth over intention）。
> 方向：深海渐进下潜（Mesophotic Deep Dive）· 用户选定 · Operate 模式 · seed key bb457d7c reroll 1

## 1. World（视觉世界）

「悦动」的每一次坚持都是一次下潜：连续打卡的天数就是潜水深度，从表层一路潜到深渊层。
界面是**潜脑仪表**——克制、无装饰、数字精确；地表是**深渊墨蓝的分层水体**，随坚持加深而逐层下沉。

- **模式**：Operate（任务型工具）。可扫读性、一致性、原生 Material 3 预期优先；品牌通过 Material 主题表达，不用装饰性表达干扰任务。
- **拒绝的品类默认**：白底蓝色圆角仪表盘圆环、黑底荧光绿"硬核健身"风、Before/After 对比图。

## 2. Color（颜色策略：Restrained + 世界分层）

**暗色 = 深渊（品牌主身份）**；**浅色 = 水面休整层（海雾灰蓝甲板）**。跟随系统，关闭动态取色（品牌一致性承诺）。

| 角色 | 暗色（深渊） | 浅色（水面休整） |
|---|---|---|
| background | `#08141F` 深渊地表 | `#E9F1F5` 海雾甲板 |
| surface | `#0C1B29` 岩壁 | `#F8FBFC` 浪尖白 |
| surfaceVariant | `#13293C` | `#DCE8EE` |
| primary / onPrimary | `#4FD8E4` 温跃层青 / `#062730` | `#0E7C8C` 深青 / 白 |
| primaryContainer | `#0E4A57` | `#C3E9F0` |
| onBackground/onSurface | `#DCE9F0` 海雪粉笔白 | `#0F2430` 墨蓝 |
| onSurfaceVariant | `#8FA9B8` 悬浮颗粒 | `#4A6372` |
| outline / outlineVariant | `#24405A` / `#16324A` | `#7C97A6` / `#B9CBD4` |
| error | `#FF6B5E` 潜脑警示红 | `#B3402F` |
| tertiary | `#3FD68F` 潜脑确认绿 | `#18845C` |

**今日页特例（diveGroundFor）**：今日页地表随连击阶段下沉（暗色 5 档：`#08141F → #07121C → #060F18 → #050C13 → #03080D`）；浅色恒定海雾甲板。其余页面恒用 colorScheme.background。

**数据可视化分类色（DiveDataColors，随主题切换）**：蛋白质蓝 `#7CB8FF/#2E7CD6`、碳水绿 `#66D98F/#1E8E4E`、脂肪 = 潜灯黄 `#FFC95C/#B26A1B`；等级三档 = 确认绿/潜灯黄/警示红。

## 3. Typography（字体）

- 中文与正文：系统 sans（设备默认字族），Material 3 角色化使用。
- **全部测量数值 = JetBrains Mono（DiveMono，自托管 Regular/Bold）**：深度带大数字、连击、打卡计数、热量/蛋白进度、体重。这是「潜表」身份的核心载体——数字是仪表读数，不是正文。
- 阶段进度等数据行使用等宽 + Bold；标签用系统 sans 的小号角色。

## 4. Shape（形状）

- 卡片 12–16dp（medium/large roles，禁止 20dp+ 的玩具圆角）
- 小控件（分段计数器段条 3dp、进度条 4dp）
- 胶囊仅用于小圆形控件；容器统一直角递进

## 5. Components（组件纪律）

- 首屏卡片配比：深度带（primaryContainer 实色）→ 训练卡 → 合并卡（饮食+体重，1dp 细线分区）——**禁止嵌套卡**，禁止 3 连同质白卡
- 底部单一主行动：随状态切换（去选计划 / 完成今日训练·下潜 / 今日已完成·明天继续-禁用态）
- 分段计数器：10 段 = 5 阶段 × 2 段，填充数 = 连击；附「距 {下一阶段} 还差 {N} 天」微标注
- 触控目标 ≥48dp；图标一律 Material Icons；条目注释一行化
- 过渡 150–250ms，运动只表达状态

## 6. Iconography & Imagery

- 动作示范：AI 生成视频（统一人物建模/蓝色短裤/纯白背景/红色肌肉高亮/5s 9:16），详情页视频优先，缩略图为视频末帧
- 火柴人动画（纯代码绘制）为视频缺失时的回退插图
- 食物：AI 生成美食图（豆包，见 provenance.md）；无 logo 位图，全部向量图标

## 7. Verification Plan（验证记录与计划）

- 已验证：真机 3B15B800EW300000 浅色/深色首屏（`.impeccable/review/phone-*.png`）、font_scale 1.3 放大无截断、合并卡与等宽数字（phone-dark.png）
- **Deferred（P2-7）**：连击达到「温跃层」（≥3 天）当日，补拍深色首屏验证 diveGroundFor 阶段下沉与更深的分段填充
- 平板/折叠屏适配未做（V1 手机优先），后续以 window size class 引入
