-- Flyway V2 migration: PSEI innovation/support tables

CREATE TABLE IF NOT EXISTS idea_registry (
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  creator_id TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  version INTEGER DEFAULT 1,
  signature TEXT,
  public_key_fingerprint TEXT
);

CREATE TABLE IF NOT EXISTS idea_access_log (
  id BIGSERIAL PRIMARY KEY,
  idea_id BIGINT REFERENCES idea_registry(id) ON DELETE CASCADE,
  accessor_id TEXT NOT NULL,
  access_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  action TEXT
);

CREATE TABLE IF NOT EXISTS patent_assistance (
  id BIGSERIAL PRIMARY KEY,
  idea_id BIGINT REFERENCES idea_registry(id) ON DELETE CASCADE,
  requester_id TEXT NOT NULL,
  status TEXT DEFAULT 'PENDING',
  prior_art_notes TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS scheme_match (
  id BIGSERIAL PRIMARY KEY,
  user_id TEXT NOT NULL,
  matched_schemes JSONB DEFAULT '[]',
  criteria JSONB DEFAULT '{}',
  matched_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS templates (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  fields JSONB DEFAULT '[]',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS mentors (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  expertise JSONB DEFAULT '[]',
  contact TEXT
);

CREATE TABLE IF NOT EXISTS investors (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  stage_focus JSONB DEFAULT '[]',
  contact TEXT
);

CREATE TABLE IF NOT EXISTS student_projects (
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  student_id TEXT NOT NULL,
  supervisor TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
