-- Flyway V3 migration: Business Identity Dashboard tables

CREATE TABLE IF NOT EXISTS businesses (
  id BIGSERIAL PRIMARY KEY,
  legal_name TEXT NOT NULL,
  registration_number TEXT UNIQUE,
  type TEXT,
  incorporation_date DATE,
  status TEXT DEFAULT 'ACTIVE',
  primary_contact TEXT,
  addresses JSONB DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS ownerships (
  id BIGSERIAL PRIMARY KEY,
  business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE,
  owner_id TEXT NOT NULL,
  owner_type TEXT,
  ownership_percentage NUMERIC,
  role TEXT,
  effective_from DATE,
  effective_to DATE
);

CREATE TABLE IF NOT EXISTS persons_entities (
  id TEXT PRIMARY KEY,
  name TEXT,
  identifier_type TEXT,
  identifier_value TEXT,
  contact TEXT
);

CREATE TABLE IF NOT EXISTS documents (
  id BIGSERIAL PRIMARY KEY,
  business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE,
  type TEXT,
  filename TEXT,
  storage_path TEXT,
  uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  uploaded_by TEXT,
  encrypted BOOLEAN DEFAULT true,
  metadata JSONB DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS role_assignments (
  id BIGSERIAL PRIMARY KEY,
  user_id TEXT NOT NULL,
  business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE,
  role TEXT,
  permissions JSONB DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGSERIAL PRIMARY KEY,
  object_type TEXT,
  object_id BIGINT,
  actor_id TEXT,
  action TEXT,
  timestamp TIMESTAMP WITH TIME ZONE DEFAULT now(),
  details TEXT
);

CREATE TABLE IF NOT EXISTS unified_identity (
  user_id TEXT PRIMARY KEY,
  verified BOOLEAN DEFAULT false,
  linked_businesses JSONB DEFAULT '[]',
  linked_properties JSONB DEFAULT '[]',
  linked_docs JSONB DEFAULT '[]'
);
