package com.flowos.app.data.repository

import com.flowos.app.data.local.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for lightweight work-context memory.
 * Stores decisions, open questions, and project goals to feed the Outcome Compiler.
 */
class FlowMemoryRepository(private val database: FlowOSDatabase) {
    
    private val goalDao = database.goalDao()
    private val outcomeDao = database.outcomeDao()
    
    fun observeActiveGoals(): Flow<List<GoalEntity>> = goalDao.observeAll()
    
    fun observeAllOutcomes(): Flow<List<OutcomeEntity>> = outcomeDao.observeAll()
    
    suspend fun getOutcome(id: String): OutcomeEntity? = outcomeDao.getById(id)
}
