import { int, mysqlEnum, mysqlTable, text, timestamp, varchar, json, boolean, date, serial, decimal, uniqueIndex } from "drizzle-orm/mysql-core";

export const users = mysqlTable("users", {
  id: varchar("id", { length: 36 }).primaryKey().default("uuid_v4()"),
  openId: varchar("openId", { length: 64 }).notNull().unique(),
  aadhaarId: varchar("aadhaar_id", { length: 12 }),
  panId: varchar("pan_id", { length: 10 }),
  username: varchar("username", { length: 255 }),
  passwordHash: varchar("password_hash", { length: 255 }),
  email: varchar("email", { length: 255 }),
  phoneNumber: varchar("phone_number", { length: 20 }),
  fullName: text("full_name"),
  name: text("name"),
  dateOfBirth: date("date_of_birth"),
  address: text("address"),
  isEmployee: boolean("is_employee").default(false),
  isActive: boolean("is_active").default(true),
  loginMethod: varchar("loginMethod", { length: 64 }),
  role: mysqlEnum("role", ["citizen", "employee", "department_admin", "system_auditor"]).default("citizen").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
  lastSignedIn: timestamp("lastSignedIn").defaultNow().notNull(),
});

export type User = typeof users.$inferSelect;
export type InsertUser = typeof users.$inferInsert;

export const roles = mysqlTable("roles", {
  id: varchar("id", { length: 36 }).primaryKey(),
  name: varchar("name", { length: 50 }).notNull().unique(),
});

export const userRoles = mysqlTable("user_roles", {
  userId: varchar("user_id", { length: 36 }).references(() => users.id, { onDelete: "cascade" }).notNull(),
  roleId: varchar("role_id", { length: 36 }).references(() => roles.id, { onDelete: "cascade" }).notNull(),
});

export const permissions = mysqlTable("permissions", {
  id: varchar("id", { length: 36 }).primaryKey(),
  name: varchar("name", { length: 100 }).notNull().unique(),
});

export const rolePermissions = mysqlTable("role_permissions", {
  roleId: varchar("role_id", { length: 36 }).references(() => roles.id, { onDelete: "cascade" }).notNull(),
  permissionId: varchar("permission_id", { length: 36 }).references(() => permissions.id, { onDelete: "cascade" }).notNull(),
});

export const administrativeLevels = mysqlTable("administrative_levels", {
  id: varchar("id", { length: 36 }).primaryKey(),
  name: varchar("name", { length: 50 }).notNull().unique(),
  parentLevelId: varchar("parent_level_id", { length: 36 }),
});

export const services = mysqlTable("services", {
  id: varchar("id", { length: 36 }).primaryKey(),
  name: varchar("name", { length: 100 }).notNull().unique(),
  description: text("description"),
  moduleType: varchar("module_type", { length: 50 }).notNull(),
  responsibleLevelId: varchar("responsible_level_id", { length: 36 }),
});

export const serviceFavorites = mysqlTable("service_favorites", {
  id: varchar("id", { length: 36 }).primaryKey(),
  userId: varchar("user_id", { length: 36 }).references(() => users.id, { onDelete: "cascade" }).notNull(),
  serviceId: varchar("service_id", { length: 36 }).references(() => services.id, { onDelete: "cascade" }).notNull(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
}, table => ({
  userServiceUnique: uniqueIndex("service_favorites_user_service_unique").on(table.userId, table.serviceId),
}));

export type ServiceFavorite = typeof serviceFavorites.$inferSelect;
export type InsertServiceFavorite = typeof serviceFavorites.$inferInsert;

export const applications = mysqlTable("applications", {
  id: varchar("id", { length: 36 }).primaryKey(),
  userId: varchar("user_id", { length: 36 }).references(() => users.id, { onDelete: "cascade" }).notNull(),
  serviceId: varchar("service_id", { length: 36 }).references(() => services.id, { onDelete: "cascade" }).notNull(),
  submissionDate: timestamp("submission_date").defaultNow().notNull(),
  status: varchar("status", { length: 50 }).notNull(),
  currentAdminLevelId: varchar("current_admin_level_id", { length: 36 }),
  applicationData: json("application_data").notNull(),
  lastUpdatedAt: timestamp("last_updated_at").defaultNow().onUpdateNow().notNull(),
});

export const modificationRequests = mysqlTable("modification_requests", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  userId: varchar("user_id", { length: 36 }).references(() => users.id, { onDelete: "cascade" }).notNull(),
  targetTable: varchar("target_table", { length: 100 }).notNull(),
  targetRecordId: varchar("target_record_id", { length: 36 }).notNull(),
  fieldName: varchar("field_name", { length: 100 }).notNull(),
  oldValue: text("old_value"),
  newValue: text("new_value").notNull(),
  requestStatus: varchar("request_status", { length: 50 }).notNull(),
  initiatedAt: timestamp("initiated_at").defaultNow().notNull(),
  approvedByUserId: varchar("approved_by_user_id", { length: 36 }),
  approvedAt: timestamp("approved_at"),
  verificationDetails: json("verification_details"),
});

export const digitalSignatures = mysqlTable("digital_signatures", {
  id: varchar("id", { length: 36 }).primaryKey(),
  entityId: varchar("entity_id", { length: 36 }).notNull(),
  entityType: varchar("entity_type", { length: 50 }).notNull(),
  userId: varchar("user_id", { length: 36 }).references(() => users.id, { onDelete: "cascade" }).notNull(),
  signatureValue: text("signature_value").notNull(),
  publicKeyFingerprint: text("public_key_fingerprint").notNull(),
  signedAt: timestamp("signed_at").defaultNow().notNull(),
  verificationStatus: varchar("verification_status", { length: 50 }).notNull(),
});

export const auditLogs = mysqlTable("audit_logs", {
  id: serial("id").primaryKey(),
  eventTimestamp: timestamp("event_timestamp").defaultNow().notNull(),
  actorUserId: varchar("actor_user_id", { length: 36 }),
  actionType: varchar("action_type", { length: 100 }).notNull(),
  targetTable: varchar("target_table", { length: 100 }),
  targetRecordId: varchar("target_record_id", { length: 36 }),
  changedData: json("changed_data"),
  administrativeLevelId: varchar("administrative_level_id", { length: 36 }),
  ipAddress: varchar("ip_address", { length: 45 }),
  sessionId: varchar("session_id", { length: 36 }),
  isTamperProof: boolean("is_tamper_proof").default(true),
});

export const marriage = mysqlTable("marriage", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  spouseAUserId: varchar("spouse_a_user_id", { length: 36 }).notNull(),
  spouseBUserId: varchar("spouse_b_user_id", { length: 36 }).notNull(),
  marriageDate: date("marriage_date").notNull(),
  registrationNumber: varchar("registration_number", { length: 255 }).unique(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const birth = mysqlTable("birth", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  childName: text("child_name").notNull(),
  motherUserId: varchar("mother_user_id", { length: 36 }).notNull(),
  fatherUserId: varchar("father_user_id", { length: 36 }),
  birthDate: date("birth_date").notNull(),
  registrationNumber: varchar("registration_number", { length: 255 }).unique(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const property = mysqlTable("property", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  ownerUserId: varchar("owner_user_id", { length: 36 }).notNull(),
  titleNumber: varchar("title_number", { length: 255 }).notNull().unique(),
  address: text("address"),
  areaSqMeters: decimal("area_sq_meters", { precision: 10, scale: 2 }),
  registrationDate: date("registration_date"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const company = mysqlTable("company", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  ownerUserId: varchar("owner_user_id", { length: 36 }).notNull(),
  companyName: text("company_name").notNull(),
  registrationNumber: varchar("registration_number", { length: 255 }).unique(),
  incorporationDate: date("incorporation_date"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const complaint = mysqlTable("complaint", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  complainantUserId: varchar("complainant_user_id", { length: 36 }).notNull(),
  againstEntity: text("against_entity"),
  description: text("description"),
  status: varchar("status", { length: 50 }).default("OPEN"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const taxRecord = mysqlTable("tax_record", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  taxpayerUserId: varchar("taxpayer_user_id", { length: 36 }).notNull(),
  year: int("year").notNull(),
  amount: decimal("amount", { precision: 10, scale: 2 }),
  paid: boolean("paid").default(false),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const ideaRegistry = mysqlTable("idea_registry", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  creatorUserId: varchar("creator_user_id", { length: 36 }).notNull(),
  title: text("title").notNull(),
  description: text("description"),
  version: int("version").default(1),
  signature: text("signature"),
  publicKeyFingerprint: text("public_key_fingerprint"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const ideaAccessLog = mysqlTable("idea_access_log", {
  id: serial("id").primaryKey(),
  ideaId: varchar("idea_id", { length: 36 }).notNull(),
  accessorUserId: varchar("accessor_user_id", { length: 36 }).notNull(),
  accessAt: timestamp("access_at").defaultNow().notNull(),
  action: text("action"),
});

export const patentAssistance = mysqlTable("patent_assistance", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  ideaId: varchar("idea_id", { length: 36 }).notNull(),
  requesterUserId: varchar("requester_user_id", { length: 36 }).notNull(),
  status: varchar("status", { length: 50 }).default("PENDING"),
  priorArtNotes: text("prior_art_notes"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const schemeMatch = mysqlTable("scheme_match", {
  id: varchar("id", { length: 36 }).primaryKey(),
  userId: varchar("user_id", { length: 36 }).notNull(),
  matchedSchemes: json("matched_schemes").default("[]").notNull(),
  criteria: json("criteria").default("{}").notNull(),
  matchedAt: timestamp("matched_at").defaultNow().notNull(),
});

export const templates = mysqlTable("templates", {
  id: varchar("id", { length: 36 }).primaryKey(),
  name: text("name").notNull(),
  description: text("description"),
  templateContent: text("template_content").notNull(),
  fields: json("fields").default("[]").notNull(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const mentors = mysqlTable("mentors", {
  id: varchar("id", { length: 36 }).primaryKey(),
  userId: varchar("user_id", { length: 36 }),
  name: text("name").notNull(),
  expertise: json("expertise").default("[]").notNull(),
  contactInfo: json("contact_info"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const investors = mysqlTable("investors", {
  id: varchar("id", { length: 36 }).primaryKey(),
  userId: varchar("user_id", { length: 36 }),
  name: text("name").notNull(),
  stageFocus: json("stage_focus").default("[]").notNull(),
  contactInfo: json("contact_info"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});

export const studentProjects = mysqlTable("student_projects", {
  id: varchar("id", { length: 36 }).primaryKey(),
  applicationId: varchar("application_id", { length: 36 }),
  studentUserId: varchar("student_user_id", { length: 36 }).notNull(),
  supervisorUserId: varchar("supervisor_user_id", { length: 36 }),
  title: text("title").notNull(),
  description: text("description"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
  updatedAt: timestamp("updated_at").defaultNow().onUpdateNow().notNull(),
});
