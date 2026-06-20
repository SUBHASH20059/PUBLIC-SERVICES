package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RegistryRecord::class, OwnershipChangeRequest::class, CourtOrder::class, BlockchainBlock::class, PropertyValuation::class],
    version = 3,
    exportSchema = false
)
abstract class RegistryDatabase : RoomDatabase() {
    abstract fun registryDao(): RegistryDao
}
