package com.flowos.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE status = 'ACTIVE' ORDER BY deadlineEpochMillis IS NULL, deadlineEpochMillis ASC, priority DESC, createdAt ASC")
    fun observeActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'DONE' ORDER BY completedAt DESC LIMIT :limit")
    fun observeCompletedTasks(limit: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'ACTIVE' AND deadlineEpochMillis IS NOT NULL AND deadlineEpochMillis >= :from ORDER BY deadlineEpochMillis ASC LIMIT :limit")
    fun observeUpcomingDeadlines(from: Long, limit: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND status = 'ACTIVE' ORDER BY deadlineEpochMillis IS NULL, deadlineEpochMillis ASC")
    fun observeProjectTasks(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    suspend fun getAllByProject(projectId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeById(taskId: String): Flow<TaskEntity?>

    @Query("UPDATE tasks SET status = 'DONE', completedAt = :completedAt WHERE id = :taskId")
    suspend fun complete(taskId: String, completedAt: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'ACTIVE' AND priority = 'HIGH'")
    fun observeHighPriorityCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'ACTIVE'")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'DONE'")
    fun observeCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countAll(): Int

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun observeById(projectId: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getById(projectId: String): ProjectEntity?

    @Query("UPDATE projects SET progressPercent = :progress WHERE id = :projectId")
    suspend fun updateProgress(projectId: String, progress: Int)

    @Query("DELETE FROM projects")
    suspend fun clearAll()
}

@Dao
interface PersonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(person: PersonEntity)

    @Query("SELECT * FROM people ORDER BY firstSeenAt DESC")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): PersonEntity?

    @Query("DELETE FROM people")
    suspend fun clearAll()
}

@Dao
interface CaptureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(capture: CaptureEntity)

    @Query("SELECT * FROM captures ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE id = :captureId")
    suspend fun getById(captureId: String): CaptureEntity?

    @Query("SELECT * FROM captures ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): CaptureEntity?

    @Query("DELETE FROM captures")
    suspend fun clearAll()
}

@Dao
interface WorkflowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<WorkflowStepEntity>)

    @Transaction
    suspend fun insertWithSteps(workflow: WorkflowEntity, steps: List<WorkflowStepEntity>) {
        insertWorkflow(workflow)
        insertSteps(steps)
    }

    @Query("SELECT * FROM workflows ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): WorkflowEntity?

    @Query("UPDATE workflows SET status = 'EXECUTED', executedAt = :executedAt WHERE id = :workflowId")
    suspend fun markExecuted(workflowId: String, executedAt: Long)

    @Query("SELECT * FROM workflows ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WorkflowEntity>>

    @Query("SELECT * FROM workflow_steps WHERE workflowId = :workflowId ORDER BY orderIndex ASC")
    suspend fun getSteps(workflowId: String): List<WorkflowStepEntity>

    @Query("SELECT * FROM workflow_steps WHERE workflowId = :workflowId ORDER BY orderIndex ASC")
    fun observeSteps(workflowId: String): Flow<List<WorkflowStepEntity>>

    @Query("UPDATE workflow_steps SET completed = :completed WHERE id = :stepId")
    suspend fun setStepCompleted(stepId: String, completed: Boolean)

    @Query("DELETE FROM workflow_steps")
    suspend fun clearSteps()

    @Query("DELETE FROM workflows")
    suspend fun clearAll()
}

@Dao
interface TaskDependencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dependencies: List<TaskDependencyEntity>)

    @Query("SELECT * FROM task_dependencies")
    fun observeAll(): Flow<List<TaskDependencyEntity>>

    @Query("SELECT * FROM task_dependencies")
    suspend fun getAll(): List<TaskDependencyEntity>

    @Query("SELECT * FROM task_dependencies WHERE fromTaskId = :taskId")
    suspend fun getDependentsOf(taskId: String): List<TaskDependencyEntity>

    @Query("DELETE FROM task_dependencies")
    suspend fun clearAll()
}

@Dao
interface ActivityEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ActivityEventEntity)

    @Query("SELECT * FROM activity_events ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ActivityEventEntity>>

    @Query("DELETE FROM activity_events")
    suspend fun clearAll()
}

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("DELETE FROM goals")
    suspend fun clearAll()
}

@Dao
interface OutcomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outcome: OutcomeEntity)

    @Update
    suspend fun update(outcome: OutcomeEntity)

    @Query("SELECT * FROM outcomes ORDER BY deadlineEpochMillis IS NULL, deadlineEpochMillis ASC")
    fun observeAll(): Flow<List<OutcomeEntity>>

    @Query("SELECT * FROM outcomes WHERE goalId = :goalId")
    fun observeByGoal(goalId: String): Flow<List<OutcomeEntity>>

    @Query("SELECT * FROM outcomes WHERE hub = :hubName")
    fun observeByHub(hubName: String): Flow<List<OutcomeEntity>>

    @Query("SELECT * FROM outcomes WHERE id = :id")
    suspend fun getById(id: String): OutcomeEntity?

    @Query("DELETE FROM outcomes")
    suspend fun clearAll()
}

@Dao
interface EvidenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(evidence: EvidenceEntity)

    @Query("SELECT * FROM evidence WHERE targetId = :targetId ORDER BY timestamp DESC")
    fun observeByTarget(targetId: String): Flow<List<EvidenceEntity>>

    @Query("DELETE FROM evidence")
    suspend fun clearAll()
}

@Dao
interface FlowScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: FlowScoreEntity)

    @Query("SELECT * FROM flow_scores ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<FlowScoreEntity?>

    @Query("DELETE FROM flow_scores")
    suspend fun clearAll()
}
