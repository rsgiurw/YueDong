package com.yuedong.fitness.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.yuedong.fitness.ui.components.BackHeader

/** 一条要点：关键词 + 说明 */
private data class GuidePoint(val key: String, val text: String)

/** 一个板块：标题 + 若干要点 */
private data class GuideSection(val title: String, val points: List<GuidePoint>)

// 指南内容（数据与 UI 分离）：单页展示，只讲要点
private val guideSections = listOf(
    GuideSection(
        "快速上手 · 三步开始",
        listOf(
            GuidePoint("选", "计划页挑一套内置计划点「设为当前」，或自建一套"),
            GuidePoint("练", "今日页照着动作清单练，练完一项勾一项"),
            GuidePoint("记", "饮食和体重随手记，坚持打卡积累连击")
        )
    ),
    GuideSection(
        "今日 · 每天的主页",
        listOf(
            GuidePoint("训练卡", "点动作名看视频示范；全部勾完自动打卡"),
            GuidePoint("饮食卡", "点「+ 记录饮食」记一笔，热量环实时更新"),
            GuidePoint("体重", "点「＋」两秒记录，趋势图随之生长"),
            GuidePoint("日记", "选心情写一句话，打卡日历会显示表情")
        )
    ),
    GuideSection(
        "计划 · 训练安排",
        listOf(
            GuidePoint("内置计划", "增肌 / 减脂 / 健康三套，带「为你推荐」标的更适合你"),
            GuidePoint("自建计划", "「＋ 自建计划」排自己的训练日、动作与组次"),
            GuidePoint("动作库", "全部动作带视频与图文；⋮ 或长按可收藏、置顶、隐藏")
        )
    ),
    GuideSection(
        "饮食 · 吃够吃对",
        listOf(
            GuidePoint("营养建议", "每日热量与三大营养素目标，附三餐搭配参考"),
            GuidePoint("记录饮食", "选食材、调克数（步进 50 克）批量记入"),
            GuidePoint("食材库", "内置常见食材，也可自建；支持收藏、置顶、隐藏")
        )
    ),
    GuideSection(
        "记录 · 你的成绩单",
        listOf(
            GuidePoint("打卡日历", "蓝环 = 已练；漏练的日子可补打卡，点某天看当日详情"),
            GuidePoint("体重趋势", "折线 + 7 日均线，范围可切换近一周到近一年"),
            GuidePoint("进步报告", "周 / 月 / 年报表，一键生成分享海报存相册")
        )
    ),
    GuideSection(
        "我的 · 个人中心",
        listOf(
            GuidePoint("修改资料", "身体数据改后热量目标立即重算"),
            GuidePoint("训练提醒", "每日定时通知，到点未打卡才会提醒"),
            GuidePoint("照片墙", "按日期存对比照，见证身材变化"),
            GuidePoint("备忘录", "训练想法随手记，删错可在回收站恢复"),
            GuidePoint("备份", "导出 zip / 导入恢复，换机前必备")
        )
    ),
    GuideSection(
        "小贴士",
        listOf(
            GuidePoint("连击", "休息日在今日页打卡，连击不断"),
            GuidePoint("批量操作", "列表长按或点行尾 ⋮，收藏 / 置顶 / 删除"),
            GuidePoint("数据隐私", "全部数据仅存本机，卸载即清除，换机先备份")
        )
    )
)

/**
 * 新手指南（我的页入口）——单页要点版：一屏纵览，条理清晰，不讲废话。
 */
@Composable
fun GuideScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        BackHeader(title = "新手指南", onBack = onBack)

        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "三分钟了解悦动：选计划 → 跟练打卡 → 记录饮食，剩下的交给坚持。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            guideSections.forEach { section ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            section.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        section.points.forEach { point ->
                            Row(Modifier.padding(vertical = 4.dp)) {
                                Box(
                                    Modifier
                                        .padding(top = 7.dp)
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    buildAnnotatedString {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(point.key)
                                            append("：")
                                        }
                                        append(point.text)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "动作示范仅供参考，请量力而行；如有不适请停止训练并咨询专业人士。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
