package com.yuedong.fitness.domain.extension

import android.content.Context
import android.net.Uri
import com.yuedong.fitness.data.db.AppDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 数据导出 / 导入(全量备份,零联网)。
 * 导出 = 各表 JSON + filesDir/media 图片,打包为一个 zip,经系统文件选择器(SAF)写到用户指定位置;
 * 导入 = 解 zip → 事务内清表重建 → 恢复媒体与配置。图片不联网,全程本地。
 */
object DataExporter {

    /** 参与备份的全部表(与 AppDatabase 实体一一对应) */
    private val TABLES = listOf(
        "user_profile", "check_in_record", "body_metric", "diary_entry", "diet_log",
        "water_log", "custom_food", "custom_exercise", "exercise_favorite",
        "list_item_flag", "workout_plan", "plan_day", "plan_exercise",
        "exercise_progress", "photo_wall", "reminders", "day_extra_exercise"
    )

    private const val MEDIA_DIR = "media"

    /** 导出为 zip 并写入目标 uri,返回导出的记录总条数(-1 = 写入失败);settings 为随包备份的偏好配置 */
    fun exportTo(context: Context, db: AppDatabase, settings: JSONObject, target: Uri): Int {
        val root = JSONObject()
        var count = 0
        val sqlite = db.openHelper.writableDatabase
        sqlite.beginTransaction()
        try {
            for (table in TABLES) {
                val arr = JSONArray()
                sqlite.query("SELECT * FROM $table").use { cursor ->
                    while (cursor.moveToNext()) {
                        val obj = JSONObject()
                        for (i in 0 until cursor.columnCount) {
                            when (cursor.getType(i)) {
                                android.database.Cursor.FIELD_TYPE_NULL -> obj.put(cursor.getColumnName(i), JSONObject.NULL)
                                android.database.Cursor.FIELD_TYPE_INTEGER -> obj.put(cursor.getColumnName(i), cursor.getLong(i))
                                android.database.Cursor.FIELD_TYPE_FLOAT -> obj.put(cursor.getColumnName(i), cursor.getDouble(i))
                                else -> obj.put(cursor.getColumnName(i), cursor.getString(i) ?: "")
                            }
                        }
                        arr.put(obj)
                        count++
                    }
                }
                root.put(table, arr)
            }
            sqlite.setTransactionSuccessful()
        } finally {
            sqlite.endTransaction()
        }
        root.put("mediaFiles", JSONArray(mediaFiles(context).map { it.name }))
        root.put("settings", settings)

        val tmp = File(context.cacheDir, backupFileName())
        ZipOutputStream(tmp.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("data.json"))
            zip.write(root.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            for (f in mediaFiles(context)) {
                zip.putNextEntry(ZipEntry("$MEDIA_DIR/${f.name}"))
                f.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        context.contentResolver.openOutputStream(target)?.use { out ->
            tmp.inputStream().use { it.copyTo(out) }
        } ?: run { tmp.delete(); return -1 }
        tmp.delete()
        return count
    }

    /** 从 uri 读取 zip 恢复:清表重建 + 恢复媒体;返回包内 settings(JSON,供 DataStore 写回) */
    fun importFrom(context: Context, db: AppDatabase, source: Uri): JSONObject {
        val tmp = File(context.cacheDir, "import_${System.currentTimeMillis()}.zip")
        context.contentResolver.openInputStream(source)?.use { input ->
            tmp.outputStream().use { input.copyTo(it) }
        } ?: throw IllegalStateException("无法读取所选文件")

        lateinit var root: JSONObject
        val mediaTmp = File(context.cacheDir, "import_media_${System.currentTimeMillis()}").apply { mkdirs() }
        try {
            ZipInputStream(tmp.inputStream().buffered()).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "data.json" ->
                            root = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                        entry.name.startsWith("$MEDIA_DIR/") && entry.name.length > MEDIA_DIR.length + 1 ->
                            File(mediaTmp, entry.name.substringAfterLast('/')).outputStream().use { zip.copyTo(it) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            val mediaDir = File(context.filesDir, MEDIA_DIR).apply { mkdirs() }
            mediaDir.listFiles()?.forEach { it.delete() }
            mediaTmp.listFiles()?.forEach { it.copyTo(File(mediaDir, it.name), overwrite = true) }
        } finally {
            tmp.delete()
            mediaTmp.deleteRecursively()
        }

        val sqlite = db.openHelper.writableDatabase
        sqlite.beginTransaction()
        try {
            // 清表按外键反向(子表先清),插入按声明正向(父表先插),避免外键校验失败
            for (table in TABLES.reversed()) {
                sqlite.execSQL("DELETE FROM $table")
            }
            for (table in TABLES) {
                val arr = root.optJSONArray(table) ?: continue
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val cv = android.content.ContentValues()
                    for (key in obj.keys()) {
                        when (val v = obj.get(key)) {
                            JSONObject.NULL -> cv.putNull(key)
                            is Int -> cv.put(key, v)
                            is Long -> cv.put(key, v)
                            is Double -> cv.put(key, v)
                            is Boolean -> cv.put(key, v)
                            else -> cv.put(key, v.toString())
                        }
                    }
                    sqlite.insert(table, 0, cv)
                }
            }
            sqlite.setTransactionSuccessful()
        } finally {
            sqlite.endTransaction()
        }
        return root.optJSONObject("settings") ?: JSONObject()
    }

    fun backupFileName(): String {
        val day = SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(Date())
        return "yuedong_backup_$day.zip"
    }

    private fun mediaFiles(context: Context): List<File> =
        File(context.filesDir, MEDIA_DIR).listFiles()?.filter { it.isFile } ?: emptyList()
}
