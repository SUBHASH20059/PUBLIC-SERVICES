-- Flyway V1 migration: initial schema for PSEI Authority

CREATE TABLE IF NOT EXISTS marriage (
  id BIGSERIAL PRIMARY KEY,
  spouse_a TEXT NOT NULL,
  spouse_b TEXT NOT NULL,
  marriage_date DATE NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS birth (
  id BIGSERIAL PRIMARY KEY,
  child_name TEXT NOT NULL,
  mother_name TEXT NOT NULL,
  father_name TEXT,
  birth_date DATE NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS property (
  id BIGSERIAL PRIMARY KEY,
  title_number TEXT UNIQUE NOT NULL,
  owner_name TEXT NOT NULL,
  address TEXT,
  area_sq_meters NUMERIC,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS company (
  id BIGSERIAL PRIMARY KEY,
  company_name TEXT NOT NULL,
  registration_number TEXT UNIQUE,
  incorporation_date DATE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS complaint (
  id BIGSERIAL PRIMARY KEY,
  complainant_name TEXT NOT NULL,
  against_name TEXT,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS tax_record (
  id BIGSERIAL PRIMARY KEY,
  taxpayer_name TEXT NOT NULL,
  year INTEGER NOT NULL,
  amount NUMERIC,
  paid BOOLEAN DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
