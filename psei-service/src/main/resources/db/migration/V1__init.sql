-- V1__init_users_and_auth.sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_roles (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(255)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- V2__init_identity_documents.sql
CREATE TYPE identity_document_type AS ENUM (
    'AADHAR_CARD', 'VOTER_ID', 'PASSPORT', 'PAN_CARD', 'DRIVING_LICENSE',
    'STATE_ID_CARD', 'AADHAAR_VIRTUAL_ID', 'UPI_ID', 'BIRTH_CERTIFICATE',
    'MARRIAGE_CERTIFICATE', 'DEATH_CERTIFICATE', 'DIVORCE_DECREE',
    'ELECTRICITY_BILL', 'WATER_BILL', 'TELEPHONE_BILL', 'GAS_BILL',
    'PROPERTY_DEED', 'RENTAL_AGREEMENT', 'EMPLOYMENT_LETTER', 'BANK_STATEMENT',
    'SALARY_SLIP', 'FORM_16', 'ITR_RETURN', 'SCHOOL_CERTIFICATE',
    'COLLEGE_DIPLOMA', 'DEGREE_CERTIFICATE', 'SKILL_CERTIFICATE',
    'JEE_SCORECARD', 'NEET_SCORECARD', 'IELTS_SCORE', 'GATE_SCORECARD',
    'VACCINATION_CERTIFICATE', 'HEALTH_INSURANCE_CARD', 'MEDICAL_REPORT',
    'DISABILITY_CERTIFICATE', 'GST_CERTIFICATE', 'IEC_CERTIFICATE',
    'UDYAM_REGISTRATION', 'SHOP_ESTABLISHMENT_LICENSE', 'SHOP_ACT_LICENSE',
    'CA_MEMBERSHIP', 'LAWYER_MEMBERSHIP', 'DOCTOR_REGISTRATION',
    'ENGINEER_REGISTRATION', 'RATION_CARD', 'SENIOR_CITIZEN_CARD',
    'BPL_CARD', 'CASTE_CERTIFICATE', 'INCOME_CERTIFICATE',
    'DOMICILE_CERTIFICATE', 'STUDENT_ID_CARD'
);

CREATE TYPE verification_status AS ENUM (
    'PENDING', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED', 'EXPIRED', 'SUSPENDED'
);

CREATE TABLE identity_documents (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    document_type identity_document_type NOT NULL,
    document_number VARCHAR(255) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    issuing_authority VARCHAR(255),
    issuer VARCHAR(255),
    state VARCHAR(255),
    holder_name VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    father_name VARCHAR(255),
    gender VARCHAR(1),
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_hash VARCHAR(64),
    encrypted_file_url TEXT,
    encryption_algorithm VARCHAR(50) DEFAULT 'AES-256-GCM',
    encryption_key_version INT DEFAULT 1,
    digital_signature TEXT,
    signature_verified BOOLEAN DEFAULT FALSE,
    verification_status verification_status DEFAULT 'PENDING',
    verification_date TIMESTAMP,
    verified_by UUID REFERENCES users(id),
    verification_notes TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_identity_documents_user_id ON identity_documents(user_id);
CREATE INDEX idx_identity_documents_status ON identity_documents(verification_status);
CREATE INDEX idx_identity_documents_type ON identity_documents(document_type);
CREATE INDEX idx_identity_documents_number ON identity_documents(document_number);

-- V3__init_government_certificates.sql
CREATE TYPE government_certificate_type AS ENUM (
    'BIRTH_REGISTRATION', 'SCHOOL_LEAVING', 'COLLEGE_DEGREE',
    'PROFESSIONAL_QUALIFICATION', 'CONTRACTOR_LICENSE',
    'HEALTH_WORKER_CERTIFICATE', 'TEACHER_CERTIFICATE',
    'POLICE_CLEARANCE', 'BUSINESS_REGISTRATION',
    'PARTNERSHIP_DEED_REGISTRATION', 'EXPORT_IMPORT_CODE',
    'FOOD_SAFETY_LICENSE', 'MEDICINE_LICENSE', 'SC_ST_OBC_CERTIFICATE',
    'INCOME_CERTIFICATE', 'WIDOW_PENSION_CERTIFICATE',
    'DISABILITY_CERTIFICATE', 'POVERTY_BPL_CARD',
    'POLLUTION_CONTROL_BOARD_APPROVAL', 'FACTORY_LICENSE',
    'BUILDING_COMPLETION_CERTIFICATE', 'LAND_OWNERSHIP_CERTIFICATE',
    'AGRICULTURAL_LOAN_CERTIFICATE', 'ORGANIC_FARMING_CERTIFICATE',
    'DRIVING_PERMISSION_CERTIFICATE', 'VACCINATION_CERTIFICATE',
    'MARRIAGE_CERTIFICATE', 'CHARACTER_CERTIFICATE',
    'STARTUP_RECOGNITION', 'PATENT_GRANT_CERTIFICATE',
    'TRADEMARK_REGISTRATION', 'GEOGRAPHICAL_INDICATION',
    'DESIGN_REGISTRATION'
);

CREATE TYPE certificate_status AS ENUM (
    'ACTIVE', 'EXPIRED', 'REVOKED', 'SUSPENDED', 'ISSUED', 'LOST', 'DUPLICATE'
);

CREATE TYPE verification_result AS ENUM (
    'PENDING', 'AUTHENTIC', 'SUSPICIOUS', 'FORGED', 'NOT_FOUND'
);

CREATE TABLE government_certificates (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    certificate_type government_certificate_type NOT NULL,
    certificate_number VARCHAR(255) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    issuing_department VARCHAR(255) NOT NULL,
    issuing_office VARCHAR(255) NOT NULL,
    issuing_officer VARCHAR(255),
    holder_name VARCHAR(255) NOT NULL,
    certificate_details JSONB DEFAULT '{}',
    file_hash VARCHAR(64) NOT NULL,
    encrypted_certificate_url TEXT NOT NULL,
    encryption_algorithm VARCHAR(50) DEFAULT 'AES-256-GCM',
    digital_signature TEXT NOT NULL,
    signature_algorithm VARCHAR(50) DEFAULT 'RSA-SHA256',
    issuer_public_key_fingerprint VARCHAR(255) NOT NULL,
    blockchain_hash VARCHAR(255),
    qr_code_url TEXT,
    status certificate_status DEFAULT 'ACTIVE',
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verification_method VARCHAR(50),
    revoked_at TIMESTAMP,
    revocation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_government_certificates_user_id ON government_certificates(user_id);
CREATE INDEX idx_government_certificates_status ON government_certificates(status);
CREATE INDEX idx_government_certificates_type ON government_certificates(certificate_type);
CREATE INDEX idx_government_certificates_number ON government_certificates(certificate_number);

-- V4__init_access_logs.sql
CREATE TABLE document_access_logs (
    id UUID PRIMARY KEY,
    document_id UUID REFERENCES identity_documents(id) ON DELETE CASCADE,
    accessor_id UUID REFERENCES users(id),
    accessor_role VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(50),
    ip_address INET,
    device_info TEXT,
    reason TEXT
);

CREATE TABLE certificate_access_logs (
    id UUID PRIMARY KEY,
    certificate_id UUID REFERENCES government_certificates(id) ON DELETE CASCADE,
    accessor_id UUID REFERENCES users(id),
    accessor_role VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(50),
    verification_result VARCHAR(50),
    ip_address INET,
    organization VARCHAR(255)
);

CREATE INDEX idx_document_access_logs_document_id ON document_access_logs(document_id);
CREATE INDEX idx_certificate_access_logs_certificate_id ON certificate_access_logs(certificate_id);

-- V5__init_certificate_sharing.sql
CREATE TYPE access_level AS ENUM ('VIEW_ONLY', 'DOWNLOAD', 'SHARE', 'VERIFY');

CREATE TABLE certificate_shares (
    id UUID PRIMARY KEY,
    certificate_id UUID REFERENCES government_certificates(id) ON DELETE CASCADE,
    owner_id UUID REFERENCES users(id),
    shared_with VARCHAR(255) NOT NULL,
    sharing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE,
    access_level access_level DEFAULT 'VIEW_ONLY',
    purpose TEXT
);

CREATE INDEX idx_certificate_shares_certificate_id ON certificate_shares(certificate_id);
CREATE INDEX idx_certificate_shares_owner_id ON certificate_shares(owner_id);

-- V6__init_certificate_verification.sql
CREATE TABLE certificate_verifications (
    id UUID PRIMARY KEY,
    certificate_id UUID REFERENCES government_certificates(id),
    requested_by UUID REFERENCES users(id),
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verification_method VARCHAR(50),
    result verification_result DEFAULT 'PENDING',
    result_date TIMESTAMP,
    verified_by VARCHAR(255),
    notes TEXT,
    blockchain_tx_hash VARCHAR(255)
);

CREATE INDEX idx_certificate_verifications_certificate_id ON certificate_verifications(certificate_id);

-- V7__init_encryption_keys.sql
CREATE TABLE encryption_keys (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    key_version INT,
    algorithm VARCHAR(50),
    key_size INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rotated_at TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    key_derivation_function VARCHAR(50),
    salt_hash VARCHAR(255),
    usage_count BIGINT DEFAULT 0
);

CREATE INDEX idx_encryption_keys_user_id ON encryption_keys(user_id);

-- V8__init_audit_and_compliance.sql
CREATE TABLE security_audits (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    audit_type VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSONB,
    severity VARCHAR(50),
    action_taken TEXT
);

CREATE TABLE compliance_records (
    id UUID PRIMARY KEY,
    document_id UUID REFERENCES identity_documents(id),
    compliance_type VARCHAR(100),
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checked_by UUID REFERENCES users(id),
    status VARCHAR(50),
    notes TEXT,
    next_audit_date DATE
);

CREATE INDEX idx_security_audits_user_id ON security_audits(user_id);
CREATE INDEX idx_security_audits_timestamp ON security_audits(timestamp);
CREATE INDEX idx_compliance_records_document_id ON compliance_records(document_id);
