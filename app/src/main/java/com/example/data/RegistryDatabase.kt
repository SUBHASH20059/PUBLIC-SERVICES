package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Registry::class,
        MfaUserAccount::class,
        Employee::class,
        ActionProposal::class,
        CivilRegistry::class,
        IssuedCertificate::class,
        IdentityDocument::class,
        GstLedger::class,
        SchemeApplication::class,
        StudentProject::class,
        AuditLog::class,
        BusinessEntity::class,
        SeedIdea::class,
        OwnershipChangeRequest::class,
        CourtOrder::class,
        BlockchainBlock::class
    ],
    version = 6,
    exportSchema = false
)
abstract class RegistryDatabase : RoomDatabase() {
    abstract fun registryDao(): RegistryDao
}
