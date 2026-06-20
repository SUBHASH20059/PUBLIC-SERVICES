package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RegistryRecord::class, 
        OwnershipChangeRequest::class, 
        CourtOrder::class, 
        BlockchainBlock::class, 
        PropertyValuation::class,
        Employee::class,
        AuditLog::class,
        SecureVaultRecord::class
    ],
    version = 4,
    exportSchema = false
)
abstract class RegistryDatabase : RoomDatabase() {
    abstract fun registryDao(): RegistryDao
}
