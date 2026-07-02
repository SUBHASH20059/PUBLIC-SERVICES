package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.*

class RegistryRepository(private val registryDao: RegistryDao) {
    
    // Check if current time is a working day (Mon-Fri)
    fun isWorkingDay(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
    }

    // Intelligent Routing: Assign task to least burdened employee in department
    suspend fun assignTaskToDepartment(dept: String, proposal: ActionProposal) {
        val employee = registryDao.getLeastBurdenedEmployee(dept)
        if (employee != null) {
            val updatedEmployee = employee.copy(currentTaskCount = employee.currentTaskCount + 1)
            registryDao.updateEmployee(updatedEmployee)
            // In a real app, you'd link the proposal to this specific employee
        }
    }

    // Departmental Isolation: Fetch only relevant data
    fun getRegistries(dept: String): Flow<List<Registry>> = registryDao.getRegistriesByDept(dept)
    fun getPendingProposals(dept: String): Flow<List<ActionProposal>> = registryDao.getPendingProposalsByDept(dept)
    fun getAuditLogs(dept: String): Flow<List<AuditLog>> = registryDao.getAuditLogsByDept(dept)

    // ... (rest of the repository methods)
}
