package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RegistryRecord::class, 
        GstProfile::class,
        InvoiceLedger::class,
        Employee::class,
        ActionProposal::class,
        CivilRegistry::class,
        IssuedCertificate::class,
        SchemeApplication::class,
        AuditLog::class,
        OwnershipChangeRequest::class, 
        CourtOrder::class, 
        BlockchainBlock::class, 
        PropertyValuation::class,
        SecureVaultRecord::class,
        BusinessEntity::class,
        SeedIdea::class,
        BusinessComplianceLog::class
    ],
    version = 6,
    exportSchema = false
)
abstract class RegistryDatabase : RoomDatabase() {
    abstract fun registryDao(): RegistryDao
}
