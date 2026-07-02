package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistryDao {
    @Query("SELECT * FROM registries WHERE department = :dept")
    fun getRegistriesByDept(dept: String): Flow<List<Registry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Query("SELECT * FROM employees WHERE department = :dept AND isActive = 1 ORDER BY currentTaskCount ASC LIMIT 1")
    suspend fun getLeastBurdenedEmployee(dept: String): Employee?

    @Query("SELECT * FROM action_proposals WHERE department = :dept AND checkerId IS NULL")
    fun getPendingProposalsByDept(dept: String): Flow<List<ActionProposal>>

    @Query("SELECT * FROM audit_logs WHERE department = :dept ORDER BY timestamp DESC")
    fun getAuditLogsByDept(dept: String): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Update
    suspend fun updateEmployee(employee: Employee)

    // ... (rest of the DAO methods)
}
