import { getDb } from "./db";
import { sql } from "drizzle-orm";

export async function migrateDatabase() {
  const db = await getDb();
  if (!db) {
    console.warn("[Migration] Database connection unavailable");
    return;
  }

  console.log("[Migration] Ensuring governance tables exist...");

  try {
    await db.execute(sql`
      CREATE TABLE IF NOT EXISTS users (
        id VARCHAR(36) PRIMARY KEY,
        openId VARCHAR(64) NOT NULL UNIQUE,
        aadhaar_id VARCHAR(12), pan_id VARCHAR(10), username VARCHAR(255), password_hash VARCHAR(255),
        email VARCHAR(255), phone_number VARCHAR(20), full_name TEXT, name TEXT, date_of_birth DATE, address TEXT,
        is_employee BOOLEAN DEFAULT FALSE, is_active BOOLEAN DEFAULT TRUE,
        loginMethod VARCHAR(64), role ENUM('citizen', 'employee', 'department_admin', 'system_auditor') DEFAULT 'citizen' NOT NULL,
        createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
        updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
        lastSignedIn TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
      );
    `);
    await db.execute(sql`
      CREATE TABLE IF NOT EXISTS administrative_levels (
        id VARCHAR(36) PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE,
        parent_level_id VARCHAR(36)
      );
    `);
    await db.execute(sql`
      CREATE TABLE IF NOT EXISTS services (
        id VARCHAR(36) PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE,
        description TEXT,
        module_type VARCHAR(50) NOT NULL,
        responsible_level_id VARCHAR(36)
      );
    `);
    await db.execute(sql`
      CREATE TABLE IF NOT EXISTS applications (
        id VARCHAR(36) PRIMARY KEY,
        user_id VARCHAR(36) NOT NULL,
        service_id VARCHAR(36) NOT NULL,
        submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
        status VARCHAR(50) NOT NULL,
        current_admin_level_id VARCHAR(36),
        application_data JSON NOT NULL,
        last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
      );
    `);
    await db.execute(sql`
      CREATE TABLE IF NOT EXISTS modification_requests (
        id VARCHAR(36) PRIMARY KEY,
        application_id VARCHAR(36), user_id VARCHAR(36) NOT NULL,
        target_table VARCHAR(100) NOT NULL, target_record_id VARCHAR(36) NOT NULL, field_name VARCHAR(100) NOT NULL,
        old_value TEXT, new_value TEXT NOT NULL, request_status VARCHAR(50) NOT NULL,
        initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, approved_by_user_id VARCHAR(36), approved_at TIMESTAMP,
        verification_details JSON
      );
    `);
    await db.execute(sql`
      CREATE TABLE IF NOT EXISTS audit_logs (
        id INT AUTO_INCREMENT PRIMARY KEY,
        event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
        actor_user_id VARCHAR(36), action_type VARCHAR(100) NOT NULL, target_table VARCHAR(100), target_record_id VARCHAR(36),
        changed_data JSON, administrative_level_id VARCHAR(36), ip_address VARCHAR(45), session_id VARCHAR(36),
        is_tamper_proof BOOLEAN DEFAULT TRUE
      );
    `);
    console.log("[Migration] Governance tables ready.");
  } catch (error) {
    console.error("[Migration] Failed to provision governance tables:", error);
  }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  migrateDatabase();
}
        
        
