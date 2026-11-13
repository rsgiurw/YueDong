package com.yuedong.fitness.domain.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import com.yuedong.fitness.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

/**
 * 应用内检查更新（自建发版通道，零第三方依赖）。
 *
 * 原理：APP 读 [UPDATE_JSON_URL] 处的 version.json（versionCode / versionName /
 * apkUrl / changelog / force），版本高于自身则弹窗引导；下载 APK 到应用私有
 * 目录，经 FileProvider 拉起系统安装器。用户数据全程不离开手机。
 *
 * 发版流程见 D:\AI项目\悦动\发布\发版指南.md：每次发新版只需构建 → 上传 APK
 * 与新的 version.json，所有已装用户下次打开 APP 即收到提示。
 */

/** 远端版本描述，即 version.json 的内容 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
    val force: Boolean
)

object AppUpdater {

    /**
     * version.json 的固定地址。
     * ⚠️ 一旦对外发布就不可随意更换（已装用户的老版本 APP 只认这个地址）。
     *   先按《发版指南》选定存储位置（OSS/COS/自有服务器），再把这里替换成
     *   实际地址后构建发版。局域网联调可临时用 http://192.168.x.x:8000/version.json。
     */
    const val UPDATE_JSON_URL = "https://REPLACE-ME.example.com/yuedong/version.json"

    /**
     * 检查更新。返回新版本信息；null = 已是最新。
     * 网络异常直接抛出，由调用方决定静默（启动检查）还是提示（手动检查）。
     */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        val obj = JSONObject(httpGetText(UPDATE_JSON_URL, timeoutMs = 10_000))
        val code = obj.optInt("versionCode", 0)
        val apkUrl = obj.optString("apkUrl", "")
        if (code > BuildConfig.VERSION_CODE && apkUrl.startsWith("http")) UpdateInfo(
            versionCode = code,
            versionName = obj.optString("versionName", code.toString()),
            apkUrl = apkUrl,
            changelog = obj.optString("changelog", ""),
            force = obj.optBoolean("force", false)
        ) else null
    }

    /**
     * 下载安装包到应用私有目录（无需存储权限，卸载自动清理）。
     * 下载前会清掉旧安装包；失败或中途取消不留半成品。
     * onProgress(已下载字节, 总字节或 null)。
     */
    suspend fun downloadApk(
        context: Context,
        info: UpdateInfo,
        onProgress: (Long, Long?) -> Unit = { _, _ -> }
    ): File? = withContext(Dispatchers.IO) {
        val dir = updateDir(context)
        val target = File(dir, "yuedong-${info.versionCode}.apk")
        try {
            dir.mkdirs()
            dir.listFiles()?.forEach { it.delete() }
            val conn = openUrl(info.apkUrl, timeoutMs = 30_000)
            val total = conn.contentLengthLong.takeIf { it > 0 }
            var done = 0L
            streamOf(conn).use { input ->
                target.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buf)
                        if (read < 0) break
                        out.write(buf, 0, read)
                        done += read
                        onProgress(done, total)
                    }
                }
            }
            target
        } catch (e: Exception) {
            target.delete()
            null
        }
    }

    /** 本地已有该版本的完整安装包则直接复用，省一次下载 */
    fun findDownloadedApk(context: Context, info: UpdateInfo): File? =
        File(updateDir(context), "yuedong-${info.versionCode}.apk")
            .takeIf { it.isFile && it.length() > 0 }

    /** 是否已获得系统的「安装未知应用」授权（Android 8 起按应用单独授权） */
    fun canInstallApk(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    /** 跳到本应用的「允许安装未知应用」系统设置页 */
    fun openInstallPermissionSetting(context: Context) {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        )
    }

    /** 用系统安装器打开已下载的 APK（安装器会校验签名，非本签名包装不上） */
    fun launchInstaller(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun updateDir(context: Context): File =
        context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "download")

    private fun openUrl(url: String, timeoutMs: Int): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = timeoutMs
        conn.readTimeout = timeoutMs
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("Accept-Encoding", "gzip")
        conn.connect()
        if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}: $url")
        return conn
    }

    private fun streamOf(conn: HttpURLConnection): InputStream =
        if (conn.contentEncoding?.equals("gzip", ignoreCase = true) == true)
            GZIPInputStream(conn.inputStream) else conn.inputStream

    private fun httpGetText(url: String, timeoutMs: Int): String =
        streamOf(openUrl(url, timeoutMs)).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
