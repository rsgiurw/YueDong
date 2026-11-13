package com.yuedong.fitness.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yuedong.fitness.data.db.dao.BodyMetricDao
import com.yuedong.fitness.data.db.dao.CheckInRecordDao
import com.yuedong.fitness.data.db.dao.CustomExerciseDao
import com.yuedong.fitness.data.db.dao.CustomFoodDao
import com.yuedong.fitness.data.db.dao.DietLogDao
import com.yuedong.fitness.data.db.dao.DiaryEntryDao
import com.yuedong.fitness.data.db.dao.ExerciseFavoriteDao
import com.yuedong.fitness.data.db.dao.ExerciseProgressDao
import com.yuedong.fitness.data.db.dao.PlanDayDao
import com.yuedong.fitness.data.db.dao.PlanExerciseDao
import com.yuedong.fitness.data.db.dao.ProfileDao
import com.yuedong.fitness.data.db.dao.MemoDao
import com.yuedong.fitness.data.db.dao.DayExtraExerciseDao
import com.yuedong.fitness.data.db.dao.RemindersDao
import com.yuedong.fitness.data.db.dao.PhotoWallDao
import com.yuedong.fitness.data.db.dao.WaterLogDao
import com.yuedong.fitness.data.db.dao.ListItemFlagDao
import com.yuedong.fitness.data.db.dao.WorkoutPlanDao
import com.yuedong.fitness.data.db.entity.BodyMetricEntity
import com.yuedong.fitness.data.db.entity.CheckInRecordEntity
import com.yuedong.fitness.data.db.entity.CustomExerciseEntity
import com.yuedong.fitness.data.db.entity.CustomFoodEntity
import com.yuedong.fitness.data.db.entity.DietLogEntity
import com.yuedong.fitness.data.db.entity.DiaryEntryEntity
import com.yuedong.fitness.data.db.entity.ExerciseFavoriteEntity
import com.yuedong.fitness.data.db.entity.ExerciseProgressEntity
import com.yuedong.fitness.data.db.entity.PlanDayEntity
import com.yuedong.fitness.data.db.entity.PlanExerciseEntity
import com.yuedong.fitness.data.db.entity.UserProfileEntity
import com.yuedong.fitness.data.db.entity.WaterLogEntity
import com.yuedong.fitness.data.db.entity.ListItemFlagEntity
import com.yuedong.fitness.data.db.entity.MemoEntity
import com.yuedong.fitness.data.db.entity.DayExtraExerciseEntity
import com.yuedong.fitness.data.db.entity.RemindersEntity
import com.yuedong.fitness.data.db.entity.PhotoWallEntity
import com.yuedong.fitness.data.db.entity.WorkoutPlanEntity

/**
 * 应用主数据库（模块契约见设计包 §8 DataLayer）。
 * 职责：持有全部表与 DAO；非职责：不含任何业务规则。
 * 版本 2：v2 新增饮食记录表与自定义食物表（见 MIGRATION_1_2，不丢失老数据）。
 */
@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutPlanEntity::class,
        PlanDayEntity::class,
        PlanExerciseEntity::class,
        CheckInRecordEntity::class,
        BodyMetricEntity::class,
        DiaryEntryEntity::class,
        DietLogEntity::class,
        CustomFoodEntity::class,
        CustomExerciseEntity::class,
        ExerciseProgressEntity::class,
        ExerciseFavoriteEntity::class,
        WaterLogEntity::class,
        ListItemFlagEntity::class,
        PhotoWallEntity::class,
        MemoEntity::class,
        RemindersEntity::class,
        DayExtraExerciseEntity::class
    ],
    version = 23,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun planDayDao(): PlanDayDao
    abstract fun planExerciseDao(): PlanExerciseDao
    abstract fun checkInRecordDao(): CheckInRecordDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun dietLogDao(): DietLogDao
    abstract fun customFoodDao(): CustomFoodDao
    abstract fun customExerciseDao(): CustomExerciseDao
    abstract fun exerciseFavoriteDao(): ExerciseFavoriteDao
    abstract fun exerciseProgressDao(): ExerciseProgressDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun photoWallDao(): PhotoWallDao
    abstract fun memoDao(): MemoDao
    abstract fun remindersDao(): RemindersDao
    abstract fun dayExtraExerciseDao(): DayExtraExerciseDao
    abstract fun listItemFlagDao(): ListItemFlagDao
}

/**
 * 版本 4 → 5：新增动作完成进度表（今日页逐项勾选，不丢失老数据）。
 */
/**
 * 版本 5 → 6：计划表新增 cycleDays（训练周期天数，支持按强度定周期与自建可调周期）。
 */
/**
 * 版本 6 → 7：新增动作收藏表（exercise_favorite）。
 */
/**
 * 版本 7 → 8：计划表新增 isFavorite（计划收藏标记）。
 */
/**
 * 版本 8 → 9：新增饮水记录表（water_log，按次记录按日汇总）。
 */
/**
 * 版本 9 → 10：自定义动作表新增难度列（入门/初级/中级/高级）。
 */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE custom_exercise ADD COLUMN difficulty TEXT NOT NULL DEFAULT '入门'")
    }
}

/**
 * 版本 10 → 11：自定义动作/自定义食物新增介绍列（description，默认空串）。
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE custom_exercise ADD COLUMN description TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE custom_food ADD COLUMN description TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * 版本 11 → 12：计划表新增置顶列（长按菜单置顶，置顶计划排列表最前）。
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_plan ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * 版本 12 → 13：新增列表条目标记表（置顶/收藏/隐藏，内置与自建条目通用）。
 */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `list_item_flag` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`itemType` TEXT NOT NULL, " +
                "`itemName` TEXT NOT NULL, " +
                "`flag` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_list_item_flag_itemType_itemName_flag` " +
                "ON `list_item_flag` (`itemType`, `itemName`, `flag`)"
        )
    }
}

/**
 * 版本 13 → 14：自定义动作新增示范视频列、自定义食物新增图片列（本地文件路径，可空）。
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE custom_exercise ADD COLUMN videoPath TEXT")
        db.execSQL("ALTER TABLE custom_food ADD COLUMN imagePath TEXT")
    }
}

/**
 * 版本 14 → 15：自定义动作新增封面图列与自定义图文段落列。
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE custom_exercise ADD COLUMN coverPath TEXT")
        db.execSQL("ALTER TABLE custom_exercise ADD COLUMN sections TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * 版本 15 → 16：自定义食物新增自定义图文段落列。
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE custom_food ADD COLUMN sections TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN avatarPath TEXT")
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `custom_exercise` ADD COLUMN `bodyPart` TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS day_extra_exercise(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "setsText TEXT NOT NULL, " +
                "createdAt INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS reminders(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "hour INTEGER NOT NULL, " +
                "minute INTEGER NOT NULL, " +
                "enabled INTEGER NOT NULL DEFAULT 1, " +
                "createdAt INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_plan ADD COLUMN deletedAt INTEGER")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE custom_food ADD COLUMN deletedAt INTEGER")
        db.execSQL("ALTER TABLE custom_exercise ADD COLUMN deletedAt INTEGER")
        db.execSQL("ALTER TABLE photo_wall ADD COLUMN deletedAt INTEGER")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS memo(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "updatedAt INTEGER NOT NULL, " +
                "deletedAt INTEGER)"
        )
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN nickname TEXT NOT NULL DEFAULT '悦动用户'")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS photo_wall(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "path TEXT NOT NULL, " +
                "createdAt INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `water_log` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`amountMl` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_water_log_date` ON `water_log` (`date`)")
    }
}
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_plan ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS exercise_favorite (" +
                "exerciseName TEXT NOT NULL PRIMARY KEY, createdAt INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_plan ADD COLUMN cycleDays INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `exercise_progress` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`planExerciseId` INTEGER NOT NULL, " +
                "`done` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_progress_date` ON `exercise_progress` (`date`)")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_progress_date_planExerciseId` " +
                "ON `exercise_progress` (`date`, `planExerciseId`)"
        )
    }
}

/**
 * 版本 3 → 4：自定义动作表补充组数与次数/时长列（已有行填默认值 1组/1次）。
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `custom_exercise` ADD COLUMN `setsText` TEXT NOT NULL DEFAULT '1组'")
        db.execSQL("ALTER TABLE `custom_exercise` ADD COLUMN `repsOrDuration` TEXT NOT NULL DEFAULT '1次'")
    }
}

/**
 * 版本 2 → 3：新增自定义动作表（计划编辑输入过的动作自动入库复用）。
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `custom_exercise` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_custom_exercise_name` ON `custom_exercise` (`name`)"
        )
    }
}

/**
 * 版本 1 → 2：新增饮食记录与自定义食物两张表。
 * SQL 必须与 Room 实体定义完全一致（列类型、非空约束、索引）。
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `diet_log` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`mealType` TEXT NOT NULL, " +
                "`foodName` TEXT NOT NULL, " +
                "`grams` REAL NOT NULL, " +
                "`calories` INTEGER NOT NULL, " +
                "`proteinG` REAL NOT NULL, " +
                "`carbG` REAL NOT NULL, " +
                "`fatG` REAL NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_diet_log_date` ON `diet_log` (`date`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `custom_food` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`kcalPer100g` INTEGER NOT NULL, " +
                "`proteinPer100g` REAL NOT NULL, " +
                "`carbPer100g` REAL NOT NULL, " +
                "`fatPer100g` REAL NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_custom_food_name` ON `custom_food` (`name`)"
        )
    }
}
