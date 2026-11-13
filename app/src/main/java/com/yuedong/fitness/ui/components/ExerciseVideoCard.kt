package com.yuedong.fitness.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * 视频版动作教学卡（v2.4）：AI 生成的动作演示视频循环播放。
 * 职责：静音循环播放、播放/暂停、后台自动暂停；非职责：不含动作讲解文字（由详情页组织）。
 * 说明：视频由用户用 AI 工具生成后打包进 res/raw，应用保持纯本地、零联网。
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoExerciseIllustrationCard(
    videoRes: Int,
    legend: String,
    modifier: Modifier = Modifier
) {
    VideoExerciseIllustrationCard(
        videoUri = RawResourceDataSource.buildRawResourceUri(videoRes),
        legend = legend,
        modifier = modifier
    )
}

/** 本地视频版：自定义动作上传的示范视频（应用私有目录内的文件路径），画面比例随视频自适应；actions 会渲染在卡片内视频下方 */
@OptIn(UnstableApi::class)
@Composable
fun VideoExerciseIllustrationCard(
    videoUri: android.net.Uri,
    legend: String,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var playing by remember { mutableStateOf(true) }
    var videoRatio by remember(videoUri) { mutableStateOf(16f / 9f) }

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            @Suppress("DEPRECATION")
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = true
            prepare()
            addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        videoRatio = (videoSize.width.toFloat() / videoSize.height.toFloat())
                            .coerceIn(0.5f, 2.2f)
                    }
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                Lifecycle.Event.ON_START -> if (playing) exoPlayer.play()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // TextureView 与视图层级正常合成，可直接释放，转场无残留
            exoPlayer.release()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        // 容器随主题（此前写死白底，暗色下 onSurface 文字不可读）；播放器表面由视频内容决定
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "动作示范",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        if (playing) exoPlayer.pause() else exoPlayer.play()
                        playing = !playing
                    }
                ) {
                    Icon(
                        if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playing) "暂停演示" else "播放演示",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            AndroidView(
                factory = { ctx ->
                    // 使用 XML 布局以启用 texture_view：普通图层合成，返回转场无画面残留
                    val view = android.view.LayoutInflater.from(ctx)
                        .inflate(com.yuedong.fitness.R.layout.view_exercise_player, null, false)
                    val playerView = view.findViewById<PlayerView>(com.yuedong.fitness.R.id.player_view)
                    playerView.player = exoPlayer
                    view
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(videoRatio)
            )
            if (actions != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) { actions() }
            }
            if (legend.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    legend,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
