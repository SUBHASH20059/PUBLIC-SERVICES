package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RegistryRepository(private val registryDao: RegistryDao) {

    val allRecords: Flow<List<RegistryRecord>> = registryDao.getAllRecords()
    val allChangeRequests: Flow<List<OwnershipChangeRequest>> = registryDao.getAllChangeRequests()
    val allCourtOrders: Flow<List<CourtOrder>> = registryDao.getAllCourtOrders()
    val allBlockchainBlocks: Flow<List<BlockchainBlock>> = registryDao.getAllBlockchainBlocks()
    val allValuations: Flow<List<PropertyValuation>> = registryDao.getAllValuations()

    fun getRecordsByOwner(uid: String): Flow<List<RegistryRecord>> = registryDao.getRecordsByOwner(uid)

    suspend fun getRecordById(id: Int): RegistryRecord? = registryDao.getRecordById(id)

    suspend fun insertRecord(record: RegistryRecord) = registryDao.insertRecord(record)

    suspend fun updateRecord(record: RegistryRecord) = registryDao.updateRecord(record)

    suspend fun insertChangeRequest(request: OwnershipChangeRequest) = registryDao.insertChangeRequest(request)

    suspend fun updateChangeRequest(request: OwnershipChangeRequest) = registryDao.updateChangeRequest(request)

    suspend fun getCourtOrderByNumber(orderNumber: String): CourtOrder? = registryDao.getCourtOrderByNumber(orderNumber)

    suspend fun insertCourtOrder(courtOrder: CourtOrder) = registryDao.insertCourtOrder(courtOrder)

    suspend fun updateCourtOrder(courtOrder: CourtOrder) = registryDao.updateCourtOrder(courtOrder)

    suspend fun insertBlockchainBlock(block: BlockchainBlock) = registryDao.insertBlockchainBlock(block)

    suspend fun getLatestBlockchainBlock(): BlockchainBlock? = registryDao.getLatestBlockchainBlock()

    suspend fun insertValuation(valuation: PropertyValuation) = registryDao.insertValuation(valuation)

    suspend fun seedDatabase() {
        val currentRecords = registryDao.getAllRecords().first()
        if (currentRecords.isEmpty()) {
            // Seed 1: Land Registry
            registryDao.insertRecord(
                RegistryRecord(
                    type = "LAND",
                    title = "Agricultural Land - Survey No. 401, Pune District",
                    ownerName = "Rajesh Kumar",
                    ownerUniqueId = "UID-8942-1029-9912",
                    description = "2.5 Acres of irrigated black cotton soil land situated at Village Narayangaon, Pune. Bound by Forest land on East and Gram Panchayat road on North.",
                    additionalParties = "Co-applicant: Sumitra Devi",
                    status = "APPROVED",
                    constitutionStatutes = "Sec 17 of The Registration Act, 1908 & Sec 54 of Transfer of Property Act, 1882",
                    iasClearance = true,
                    incomeTaxClearance = true,
                    courtOrderLinked = "COURT/ORD/2026/01",
                    chargeAmount = 0.0,
                    verifiedByOfficer = "IAS Officer - Sub Divisional Magistrate"
                )
            )

            // Seed Genesis Block & Land Approval Block
            val genesisPayload = "{\"event\": \"GENESIS\", \"desc\": \"Lekha Civil Trust Sovereign Block Ledger Initialized with Zero-Trust Protocol\"}"
            registryDao.insertBlockchainBlock(
                BlockchainBlock(
                    blockIndex = 1,
                    previousHash = "0000000000000000000000000000000000000000000000000000000000000000",
                    hash = "000000c0a81119b9d3bdf90bc3d2be3678cd84c49f8fb4468f307e5595914fa7",
                    recordId = 0,
                    transactionType = "GENESIS",
                    payload = genesisPayload,
                    nonce = 1042
                )
            )

            val record1Payload = "{\"event\": \"DEED_APPROVED\", \"recordId\": 1, \"title\": \"Agricultural Land - Survey No. 401, Pune District\", \"owner\": \"Rajesh Kumar\", \"ownerUid\": \"UID-8942-1029-9912\"}"
            registryDao.insertBlockchainBlock(
                BlockchainBlock(
                    blockIndex = 2,
                    previousHash = "000000c0a81119b9d3bdf90bc3d2be3678cd84c49f8fb4468f307e5595914fa7",
                    hash = "000000f5c1815db9766cd9a3cbcfac1bba6dabcdeaa0c8223af043fbdf981ac6",
                    recordId = 1,
                    transactionType = "DEED_GEN",
                    payload = record1Payload,
                    nonce = 8492
                )
            )

            // Seed some Property Valuations
            registryDao.insertValuation(
                PropertyValuation(
                    propertyName = "Pune Survey No. 401 Land Parcel",
                    surveyorName = "Registrar Office Evaluator",
                    zoneClassification = "Agricultural",
                    regionalGuidelineRate = 1200.0,
                    landAreaSqFt = 108900.0, // 2.5 Acres
                    developmentalPremiumMultiplier = 1.25,
                    overallAssessedValue = 163350000.0, // Area * Guideline * Multiplier
                    blockchainSealHash = "000000abc18429deffac8932bc3df92dca32ff09e99a82cd93bbf6e60b61acfa"
                )
            )

            registryDao.insertValuation(
                PropertyValuation(
                    propertyName = "Cybercity Office Block B - IT Suite",
                    surveyorName = "Municipal Assessor Office",
                    zoneClassification = "Premium Commercial",
                    regionalGuidelineRate = 11500.0,
                    landAreaSqFt = 4500.0,
                    developmentalPremiumMultiplier = 1.95,
                    overallAssessedValue = 100912500.0,
                    blockchainSealHash = "000000ef8542cd93bbca92437bf2389ba2cde82eff762dac894fcbeecb916abc"
                )
            )

            // Seed 2: Marriage Registry
            registryDao.insertRecord(
                RegistryRecord(
                    type = "MARRIAGE",
                    title = "Marriage Registration: Rohan Deshmukh & Asha Kelkar",
                    ownerName = "Rohan Deshmukh",
                    ownerUniqueId = "UID-2201-8420-1123",
                    description = "Marriage solemnized under civil contract on June 5th, 2026. Joint certificate request under the Special Marriage Act. Both parties major.",
                    additionalParties = "Spouse: Asha Kelkar, Witness 1: Vijay Joshi, Witness 2: Priya Sen",
                    status = "APPROVED",
                    constitutionStatutes = "Section 13 of The Special Marriage Act, 1954 & Art. 21 right to marry of the Constitution",
                    iasClearance = true,
                    incomeTaxClearance = true,
                    courtOrderLinked = null,
                    chargeAmount = 0.0,
                    verifiedByOfficer = "IAS Officer - District Registrar"
                )
            )

            // Seed 3: Agreement (Pending)
            registryDao.insertRecord(
                RegistryRecord(
                    type = "AGREEMENT",
                    title = "Commercial Lease Agreement - Block B, Cybercity",
                    ownerName = "Sneha Grover",
                    ownerUniqueId = "UID-7762-9011-4820",
                    description = "5-Year commercial lease agreement for IT infrastructure office, carpet space 4,500 sqft in DLF Cybercity. Tenant: TechVantage Solutions Pvt Ltd.",
                    additionalParties = "Tenant: TechVantage Ltd (Repo. by Manoj Seth), Broker: HomeRun Realtors",
                    status = "PENDING",
                    constitutionStatutes = "Sec 107 of Transfer of Property Act, 1882 & Sec 10 of Indian Contract Act, 1872",
                    iasClearance = false, // Awaiting verification
                    incomeTaxClearance = false, // Awaiting IT clearance
                    courtOrderLinked = null,
                    chargeAmount = 0.0,
                    verifiedByOfficer = null
                )
            )

            // Seed 4: Loan Agreement
            registryDao.insertRecord(
                RegistryRecord(
                    type = "LOAN",
                    title = "Secured Business Facility - State Bank of India",
                    ownerName = "Gopal Sharma",
                    ownerUniqueId = "UID-3344-5566-7788",
                    description = "Collateralized loan agreement for manufacturing unit construction. Charge created over Plot 108 Industrial Area. Total Limit: ₹2.5 Crore.",
                    additionalParties = "Lender: State Bank of India, Securitisation Trustee: Indian Trusts Co.",
                    status = "APPROVED",
                    constitutionStatutes = "Indian Stamp Act, 1899 (Art. 40) & Sec 58(f) of Transfer of Property Act (Equitable Mortgage)",
                    iasClearance = true,
                    incomeTaxClearance = true,
                    courtOrderLinked = null,
                    chargeAmount = 25000000.0, // Preloaded ₹2.5 crore charge! Can ONLY be adjusted by a Court Order
                    verifiedByOfficer = "IAS Officer - Joint Registrar"
                )
            )

            // Seed 5: A Pending Land Deed Request
            registryDao.insertRecord(
                RegistryRecord(
                    type = "LAND",
                    title = "Residential Flat 402 - Rosewood Apartments, Bangalore",
                    ownerName = "Vikram Aditya",
                    ownerUniqueId = "UID-1212-3434-5656",
                    description = "Double BHK flat, super built-up 1,200 sqft with car parking spot P-12. Under development by Prestige Construction.",
                    additionalParties = "Seller: Prestige Builders Group",
                    status = "PENDING",
                    constitutionStatutes = "Section 17(1) of The Registration Act, 1908 & RERA Act, 2016",
                    iasClearance = false,
                    incomeTaxClearance = false,
                    courtOrderLinked = null,
                    chargeAmount = 0.0,
                    verifiedByOfficer = null
                )
            )

            // Preload 1 Court Order that can be executed to show modification flow
            registryDao.insertCourtOrder(
                CourtOrder(
                    orderNumber = "HC-MUM-CIVIL-2026-6712",
                    courtName = "Honorable High Court of Bombay",
                    details = "Mandatory transfer of Land Survey No. 401 Pune from Rajesh Kumar to Asha Kelkar. Decreed after adjudication of family partition suit civil appeal 129 of 2024.",
                    recordId = 1, // Target record: Agricultural Land Survey 401 Pune
                    mandatedNewOwnerName = "Asha Kelkar",
                    mandatedNewOwnerUniqueId = "UID-2201-8420-1123",
                    mandatedCharge = 0.0,
                    isExecuted = false
                )
            )

            registryDao.insertCourtOrder(
                CourtOrder(
                    orderNumber = "SC-CIVIL-DECREE-2026-409",
                    courtName = "Honorable Supreme Court of India",
                    details = "Relief of loan charge reduction on State Bank of India record. Reduce collateral charge amount down to ₹1.5 Crore due to principal recovery.",
                    recordId = 4, // Target record: Secured Business Facility - SBI
                    mandatedNewOwnerName = null,
                    mandatedNewOwnerUniqueId = null,
                    mandatedCharge = 15000000.0, // Reduced from 2.5 Crore to 1.5 Crore
                    isExecuted = false
                )
            )
        }
    }
}
