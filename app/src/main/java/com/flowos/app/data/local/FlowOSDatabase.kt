package com.flowos.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TaskEntity::class,
        ProjectEntity::class,
        PersonEntity::class,
        CaptureEntity::class,
        WorkflowEntity::class,
        WorkflowStepEntity::class,
        ActivityEventEntity::class,
        TaskDependencyEntity::class,
        GoalEntity::class,
        OutcomeEntity::class,
        EvidenceEntity::class,
        FlowScoreEntity::class,
        com.flowos.app.planner.RoutineEntity::class,
        com.flowos.app.planner.RoutineBlockEntity::class,
        com.flowos.app.planner.RoutineOccurrenceEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class FlowOSDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun personDao(): PersonDao
    abstract fun captureDao(): CaptureDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun activityEventDao(): ActivityEventDao
    abstract fun taskDependencyDao(): TaskDependencyDao
    abstract fun goalDao(): GoalDao
    abstract fun outcomeDao(): OutcomeDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun flowScoreDao(): FlowScoreDao
    abstract fun routineDao(): com.flowos.app.planner.RoutineDao

    companion object {
        @Volatile
        private var instance: FlowOSDatabase? = null

        fun get(context: Context): FlowOSDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlowOSDatabase::class.java,
                    "flowos.db",
                ).addMigrations(MIGRATION_5_6)
                    .build().also { instance = it }
            }

        /** Preserves all existing outcomes while aligning the lifecycle with the MVP. */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE outcomes ADD COLUMN completedAt INTEGER")
                db.execSQL("UPDATE outcomes SET status = 'ACTIVE' WHERE status IN ('PLANNED', 'IN_PROGRESS', 'EVIDENCE_ATTACHED')")
                db.execSQL("UPDATE outcomes SET status = 'COMPLETED', completedAt = updatedAt WHERE status = 'VERIFIED' OR progressPercent >= 100")
            }
        }
    }
}
