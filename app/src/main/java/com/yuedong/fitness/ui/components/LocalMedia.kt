package com.yuedong.fitness.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 本地媒体存取：自定义动作的示范视频、自定义食物的图片。
 * 选择后复制进应用私有目录（filesDir/media）：卸载即清理、无外链依赖，路径存数据库。
 */
object AppMedia {

    /** 把相册选择的内容拷进私有目录，返回绝对路径；失败返回 null */
    suspend fun copyFromUri(context: Context, uri: Uri, video: Boolean): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, "media").apply { mkdirs() }
                val ext = when (context.contentResolver.getType(uri)) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    "image/jpeg", "image/jpg" -> "jpg"
                    "video/mp4" -> "mp4"
                    "video/quicktime" -> "mov"
                    "video/webm" -> "webm"
                    else -> if (video) "mp4" else "jpg"
                }
                val file = File(dir, "${System.currentTimeMillis()}_${(0..999).random()}.$ext")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                } ?: return@runCatching null
                file.absolutePath
            }.getOrNull()
        }

    /** 删除私有目录里的媒体文件（路径不在私有目录时忽略） */
    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    /** 从相册内容解码位图（最长边约 maxDim），供裁剪使用 */
    suspend fun loadBitmap(context: Context, uri: Uri, maxDim: Int = 2048): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= maxDim || bounds.outHeight / (sample * 2) >= maxDim) {
                    sample *= 2
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                }
            }.getOrNull()
        }

    /** 位图存入私有目录（JPEG 92），返回绝对路径 */
    fun saveBitmap(context: Context, bitmap: Bitmap): String? = runCatching {
        val dir = File(context.filesDir, "media").apply { mkdirs() }
        val file = File(dir, "${System.currentTimeMillis()}_${(0..999).random()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        file.absolutePath
    }.getOrNull()
}

/**
 * 按路径渲染本地图片：后台解码 + 降采样（最长边约 1280），加载完成前显示空占位。
 * path 为空/解码失败时渲染空 Box（由外层控制占位样式）。
 */
@Composable
fun LocalMediaImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, path) {
        if (path.isNullOrBlank()) {
            value = null
        } else {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(path, bounds)
                    var sample = 1
                    while (bounds.outWidth / (sample * 2) >= 1280 || bounds.outHeight / (sample * 2) >= 1280) {
                        sample *= 2
                    }
                    BitmapFactory.decodeFile(
                        path,
                        BitmapFactory.Options().apply {
                            inSampleSize = sample
                            inPreferredConfig = Bitmap.Config.RGB_565
                        }
                    )?.asImageBitmap()
                }.getOrNull()
            }
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(modifier)
    }
}
