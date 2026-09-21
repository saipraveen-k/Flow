package com.flowos.app.planner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<RoutineBlockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: RoutineOccurrenceEntity)

    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultRoutine(): RoutineEntity?

    @Query("SELECT * FROM routine_blocks WHERE routineId = :routineId ORDER BY startHour ASC, startMinute ASC")
    suspend fun getBlocksForRoutine(routineId: String): List<RoutineBlockEntity>

    @Query("SELECT * FROM routine_occurrences WHERE dateString = :dateString")
    suspend fun getOccurrencesForDate(dateString: String): List<RoutineOccurrenceEntity>

    @Transaction
    suspend fun seedBuiltInRoutinesIfEmpty() {
        // Will be called by RoutineEngine if database has no routines
    }
}
