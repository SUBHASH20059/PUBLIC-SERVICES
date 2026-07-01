# 🇮🇳 PSEI Authority — National Digital Trust Platform

> **Public Services Enterprise Innovation** — A comprehensive, government-grade digital trust
> platform for 1.4 billion Indian citizens, built with Kotlin + Ktor.
>
> **Submitted to: India Runs Hackathon 2025**

---

## 📊 Platform at a Glance

| Metric | Value |
|--------|-------|
| **API Endpoints** | 70+ REST endpoints |
| **Platform Features** | 100+ across 5 tiers |
| **Identity Document Types** | 55+ Indian ID types |
| **Government Certificate Types** | 44+ with digital signatures |
| **Sector Modules** | Agriculture · Education · Healthcare · MSME · Governance |
| **Security Layers** | 15 (AES-256-GCM, HSM, ZKP, Ed25519, Argon2id, Shamir SSS) |
| **AI/ML Models** | 5 registered (Forgery · Recommendation · Intent · Anomaly · Eligibility) |
| **Languages (WhatsApp Bot)** | 8 Indian languages |
| **Target Scale** | 1.4 Billion Indian Citizens |

---

## 🏗️ Architecture

```
psei-service/src/main/kotlin/org/psei/
├── auth/              # JWT RS256, BCrypt/Argon2id, RBAC 4-tier
├── security/          # HSM DEK/KEK, ZKP age proofs, Ed25519, Shamir SSS, circuit breaker
├── ml/                # Forgery detection (CNN+LSTM+XGBoost), scheme recommendation engine
├── offline/           # CRDT Merkle-DAG sync, WhatsApp bot (8 Indian languages)
├── pipeline/          # Kafka event streaming, feature engineering, model serving, drift detection
├── features/          # Aadhaar Bridge 2.0, Face Auth, Voice Biometric, Family KYC, Health Score
├── sector/            # Agriculture, Education, Healthcare, MSME, Governance modules
├── models/            # All domain models (55+ ID types, 44+ cert types, property laws)
├── services/          # Business logic: vault, forgery, property, ID applications
└── routes/            # 70+ API endpoints across 15 route groups
```

---

## 🔒 Security Architecture

- **HSM Envelope Encryption** — DEK/KEK pattern: Data Encryption Keys wrapped by Key Encryption Keys inside HSM (AWS CloudHSM / NIC HSM). Raw DEK zeroized from memory after every use.
- **Zero-Knowledge Proofs** — Prove age ≥ 18 via Pedersen commitment — birthdate never revealed to verifier.
- **Argon2id Password Hashing** — OWASP 2024 recommended (m=64MB, t=3, p=4) replacing PBKDF2.
- **Ed25519 Request Signing** — 100× faster than RSA-4096 for service-mesh authentication and W3C Verifiable Credentials.
- **Shamir's Secret Sharing** — Master key split 3-of-5 trustees. No single point of compromise.
- **AES-256-GCM** — Per-document encryption with unique IV per operation.
- **Token Bucket Rate Limiting** — 100 burst, 10 req/s per citizen ID.
- **Circuit Breaker** — 5-failure threshold, 30s recovery for all external government API calls.

---

## 🤖 AI/ML Pipeline

```
Data Ingestion (Kafka)
    → Feature Engineering (Spark + Feast + pgvector)
        → Model Training (PyTorch + TensorFlow + XGBoost)
            → Model Serving (Triton + KServe + Ray Serve)
                → Drift Monitoring (PSI + KS test)
                    → Automated Retraining (Kubeflow Pipelines + MLflow)
                        → Feedback Loop (Citizen + Officer + Automated)
```

**5 Registered Models:**

| Model | Architecture | Task | Accuracy |
|-------|-------------|------|----------|
| `forgery_detection_v2` | CNN (ResNet-50) + LSTM + XGBoost Ensemble | Document authenticity | 94% |
| `scheme_recommendation_v1` | ALS Collaborative + Content-based + Rules | Top-N scheme matching | 87% |
| `intent_classification_v1` | IndicBERT fine-tuned multilingual | WhatsApp intent parsing | 92% |
| `anomaly_detection_v1` | Isolation Forest + LSTM Autoencoder | Login anomaly detection | 89% |
| `eligibility_scoring_v1` | XGBoost + deterministic rule engine | Scheme eligibility prob | 95% |

---

## 📱 WhatsApp Bot — 8 Indian Languages

Citizens interact via WhatsApp — no app install required. The bot supports:

| Language | Script | Intent Example |
|----------|--------|----------------|
| Hindi | हिंदी | `स्कीम बताओ` → Scheme discovery |
| Telugu | తెలుగు | `పథకం అడగండి` → Scheme request |
| Tamil | தமிழ் | `திட்டம் கேளுங்கள்` → Scheme info |
| Kannada | ಕನ್ನಡ | `ಯೋಜನೆ ಕೇಳಿ` → Scheme lookup |
| Bengali | বাংলা | `প্রকল্প জানুন` → Scheme discovery |
| Gujarati | ગુજરાતી | `યોજના` → Scheme info |
| Marathi | मराठी | `योजना सांगा` → Scheme listing |
| English | Latin | `show me schemes` → Scheme match |

---

## 🌾 Sector Modules

### Agriculture (400M+ Farmers)
- **Soil Health Passport** — Link soil card to land records with AI crop recommendations
- **Mandi Price Predictor** — AI forecast for 3,000+ APMC markets (7-day & 30-day predictions)
- **Crop Insurance Auto-Claim** — Satellite + weather data triggers payout automatically
- **Warehouse Receipt Finance** — Digital crop pledge as bank loan collateral
- **FPO Suite** — Farmer Producer Org registration, collective bargaining, export documents

### Education (300M+ Students)
- **Skill Passport** — Stackable micro-credentials with NSDC + ABC integration
- **Scholarship Radar** — Auto-match 1,000+ scholarships with eligibility scoring
- **Placement Predictor** — Skill gap analysis + salary range prediction
- **Fake University Detector** — Cross-check with UGC/AICTE registry

### Healthcare (1.4B Citizens)
- **ABHA Integration** — Ayushman Bharat Health Account with full health records
- **Vaccination Passport** — COVID + routine immunization + booster alerts
- **Organ Donor Registry** — Consent management + NOTTO registration + matching
- **Blood Bank Network** — Real-time inventory + emergency donor matching

### MSME/Business (63M MSMEs)
- **GST Compliance Copilot** — Auto-file returns, detect invoice mismatches
- **Tender Discovery AI** — Match capabilities with GeM + state portal tenders
- **Export Readiness Score** — Gap analysis for IEC, APEDA, FSSAI, quality certs
- **Patent Mining** — Auto-detect patentable innovations from business data

### Governance (20M+ Officers)
- **RTI Automation** — Auto-route, track, redact, respond to RTI requests
- **Revenue Intelligence** — Property tax evasion + benami property detection
- **Citizen Satisfaction Pulse** — Real-time NPS with low-score escalation
- **Workload Balancer AI** — Auto-distribute cases by officer capacity

---

## 🌐 API Endpoints (70+)

| Route Group | Purpose | Auth |
|-------------|---------|------|
| `/auth/**` | Register, login, refresh, roles | Partial |
| `/v1/citizens/{id}/vault` | HATEOAS document vault | JWT |
| `/v1/citizens/{id}/eligible-schemes` | AI scheme matching | JWT |
| `/v1/citizens/{id}/verifiable-credentials` | W3C VC with Ed25519 | JWT |
| `/psei/ideas/**` | Idea vault + digital signatures | JWT |
| `/psei/patents/**` | Patent filing + tracking | JWT |
| `/psei/schemes/**` | Discovery + apply | Partial |
| `/psei/business/**` | Registration + approval | JWT |
| `/psei/network/**` | Mentors + investors | Partial |
| `/psei/student/**` | Projects + grants | JWT |
| `/citizen-vault/identity-documents/**` | 55+ ID types + ML forgery | JWT |
| `/citizen-vault/certificates/**` | 44+ cert types + ZKP | JWT |
| `/citizen-services/id-applications/**` | New ID 7-step workflow | JWT |
| `/citizen-services/document-updates/**` | OTP-verified updates | JWT |
| `/citizen-services/property/**` | 5 property acts | JWT |
| `/ai/documents/{id}/analyse-forgery` | CNN+LSTM+XGBoost ensemble | JWT OFFICER+ |
| `/ai/schemes/discover` | Natural language AI | JWT |
| `/zkp/age-proof` + `/zkp/verify` | Zero-knowledge proofs | JWT/Public |
| `/whatsapp/webhook` | Bot in 8 languages | Meta Signed |
| `/sync/push` + `/sync/status` | CRDT offline sync | JWT |
| `/pipeline/**` | Kafka, drift, retraining | JWT ADMIN+ |
| `/agriculture/**` | Sector module | JWT |
| `/education/**` | Sector module | JWT |
| `/healthcare/**` | Sector module | JWT |
| `/governance/**` | Sector module | JWT OFFICER+ |
| `/openapi.json` + `/docs` | OpenAPI v3 + RapiDoc | Public |

---

## 🔐 Role Permissions Matrix

| Action | CITIZEN | OFFICER | ADMIN | SUPER_ADMIN |
|--------|:-------:|:-------:|:-----:|:-----------:|
| Upload documents | ✅ | ✅ | ✅ | ✅ |
| Verify documents | ❌ | ✅ | ✅ | ✅ |
| Run ML forgery analysis | ❌ | ✅ | ✅ | ✅ |
| Issue government certificates | ❌ | ✅ | ✅ | ✅ |
| Revoke certificates | ❌ | ❌ | ✅ | ✅ |
| View audit logs | ❌ | ❌ | ✅ | ✅ |
| Manage ML pipeline | ❌ | ❌ | ✅ | ✅ |
| Promote user roles | ❌ | ❌ | ✅* | ✅ |
| System administration | ❌ | ❌ | ❌ | ✅ |

*ADMIN can promote up to ADMIN; only SUPER_ADMIN can assign SUPER_ADMIN.

---

## 🚀 Quick Start

```bash
# 1. Clone
git clone https://github.com/SUBHASH20059/PUBLIC-SERVICES.git
cd PUBLIC-SERVICES/psei-service

# 2. Configure
export JWT_SECRET="your-256-bit-random-secret"
export DATABASE_URL="postgresql://user:pass@localhost:5432/psei"
export WHATSAPP_VERIFY_TOKEN="psei-whatsapp-verify"

# 3. Build
./gradlew build

# 4. Run
java -jar build/libs/psei-service.jar

# 5. Test
curl http://localhost:8080/health
curl http://localhost:8080/api-docs
curl http://localhost:8080/openapi.json
```

**Default admin:** `admin@psei.gov.in` / `Admin@1234`

---

## 🗄️ Database Schema (18 tables)

Flyway auto-runs migrations on startup. Tables include:
`users`, `user_roles`, `identity_documents`, `document_access_logs`,
`government_certificates`, `certificate_access_logs`, `certificate_shares`,
`certificate_verifications`, `encryption_keys`, `security_audits`,
`compliance_records` + feature-specific tables for ideas, patents, businesses, students.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9.10 |
| Framework | Ktor 2.3.4 |
| Auth | JWT RS256 + Argon2id |
| Encryption | AES-256-GCM + RSA-4096 + Ed25519 |
| Advanced Security | HSM (PKCS#11), ZKP (Pedersen), Shamir SSS |
| Database | PostgreSQL 15 + Exposed ORM + Flyway |
| Caching | Redis Cluster |
| Event Streaming | Apache Kafka / Redpanda |
| ML Serving | NVIDIA Triton + KServe + Ray Serve |
| ML Training | PyTorch + TensorFlow + XGBoost |
| ML Ops | MLflow + Kubeflow Pipelines |
| Feature Store | Feast + pgvector |
| Build | Gradle with Kotlin DSL |

---

## 📄 Hackathon Submission

See `docs/PSEI_Authority_India_Runs_Hackathon_FINAL.pdf` for the complete
hackathon submission document covering all 100 features, ML pipeline,
security architecture, sector impact, and roadmap.

---

## 📍 Roadmap

| Phase | Status | Deliverable |
|-------|--------|-------------|
| Phase 1 | ✅ Complete | Core API: Auth, Ideas, Patents, Schemes, Business |
| Phase 2 | ✅ Complete | Document Vault, Certificates, RBAC, Audit |
| Phase 3 | ✅ Complete | ID Applications, Property Laws (5 Acts) |
| Phase 4 | ✅ Complete | HSM, ZKP, Ed25519, ML Ensemble, CRDT, WhatsApp Bot |
| Phase 5 | ✅ Complete | 100+ features: Agriculture, Education, Healthcare, MSME |
| Phase 6 | Q3 2025 | UIDAI live, IP India API, GST Network, Flutter app |
| Phase 7 | Q4 2025 | Face Auth liveness, Voice Biometric, Offline Aadhaar |
| Phase 8 | 2026 | Hyperledger Fabric, Carbon Credits, 5-region deployment |

---

*Built for All Indians. Secure. Transparent. Trusted.* 🇮🇳
