CREATE TABLE `administrative_levels` (
	`id` varchar(36) NOT NULL,
	`name` varchar(50) NOT NULL,
	`parent_level_id` varchar(36),
	CONSTRAINT `administrative_levels_id` PRIMARY KEY(`id`),
	CONSTRAINT `administrative_levels_name_unique` UNIQUE(`name`)
);
--> statement-breakpoint
CREATE TABLE `applications` (
	`id` varchar(36) NOT NULL,
	`user_id` varchar(36) NOT NULL,
	`service_id` varchar(36) NOT NULL,
	`submission_date` timestamp NOT NULL DEFAULT (now()),
	`status` varchar(50) NOT NULL,
	`current_admin_level_id` varchar(36),
	`application_data` json NOT NULL,
	`last_updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `applications_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `audit_logs` (
	`id` serial AUTO_INCREMENT NOT NULL,
	`event_timestamp` timestamp NOT NULL DEFAULT (now()),
	`actor_user_id` varchar(36),
	`action_type` varchar(100) NOT NULL,
	`target_table` varchar(100),
	`target_record_id` varchar(36),
	`changed_data` json,
	`administrative_level_id` varchar(36),
	`ip_address` varchar(45),
	`session_id` varchar(36),
	`is_tamper_proof` boolean DEFAULT true,
	CONSTRAINT `audit_logs_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `birth` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`child_name` text NOT NULL,
	`mother_user_id` varchar(36) NOT NULL,
	`father_user_id` varchar(36),
	`birth_date` date NOT NULL,
	`registration_number` varchar(255),
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `birth_id` PRIMARY KEY(`id`),
	CONSTRAINT `birth_registration_number_unique` UNIQUE(`registration_number`)
);
--> statement-breakpoint
CREATE TABLE `company` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`owner_user_id` varchar(36) NOT NULL,
	`company_name` text NOT NULL,
	`registration_number` varchar(255),
	`incorporation_date` date,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `company_id` PRIMARY KEY(`id`),
	CONSTRAINT `company_registration_number_unique` UNIQUE(`registration_number`)
);
--> statement-breakpoint
CREATE TABLE `complaint` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`complainant_user_id` varchar(36) NOT NULL,
	`against_entity` text,
	`description` text,
	`status` varchar(50) DEFAULT 'OPEN',
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `complaint_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `digital_signatures` (
	`id` varchar(36) NOT NULL,
	`entity_id` varchar(36) NOT NULL,
	`entity_type` varchar(50) NOT NULL,
	`user_id` varchar(36) NOT NULL,
	`signature_value` text NOT NULL,
	`public_key_fingerprint` text NOT NULL,
	`signed_at` timestamp NOT NULL DEFAULT (now()),
	`verification_status` varchar(50) NOT NULL,
	CONSTRAINT `digital_signatures_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `idea_access_log` (
	`id` serial AUTO_INCREMENT NOT NULL,
	`idea_id` varchar(36) NOT NULL,
	`accessor_user_id` varchar(36) NOT NULL,
	`access_at` timestamp NOT NULL DEFAULT (now()),
	`action` text,
	CONSTRAINT `idea_access_log_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `idea_registry` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`creator_user_id` varchar(36) NOT NULL,
	`title` text NOT NULL,
	`description` text,
	`version` int DEFAULT 1,
	`signature` text,
	`public_key_fingerprint` text,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `idea_registry_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `investors` (
	`id` varchar(36) NOT NULL,
	`user_id` varchar(36),
	`name` text NOT NULL,
	`stage_focus` json NOT NULL DEFAULT ('[]'),
	`contact_info` json,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `investors_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `marriage` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`spouse_a_user_id` varchar(36) NOT NULL,
	`spouse_b_user_id` varchar(36) NOT NULL,
	`marriage_date` date NOT NULL,
	`registration_number` varchar(255),
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `marriage_id` PRIMARY KEY(`id`),
	CONSTRAINT `marriage_registration_number_unique` UNIQUE(`registration_number`)
);
--> statement-breakpoint
CREATE TABLE `mentors` (
	`id` varchar(36) NOT NULL,
	`user_id` varchar(36),
	`name` text NOT NULL,
	`expertise` json NOT NULL DEFAULT ('[]'),
	`contact_info` json,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `mentors_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `modification_requests` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`user_id` varchar(36) NOT NULL,
	`target_table` varchar(100) NOT NULL,
	`target_record_id` varchar(36) NOT NULL,
	`field_name` varchar(100) NOT NULL,
	`old_value` text,
	`new_value` text NOT NULL,
	`request_status` varchar(50) NOT NULL,
	`initiated_at` timestamp NOT NULL DEFAULT (now()),
	`approved_by_user_id` varchar(36),
	`approved_at` timestamp,
	`verification_details` json,
	CONSTRAINT `modification_requests_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `patent_assistance` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`idea_id` varchar(36) NOT NULL,
	`requester_user_id` varchar(36) NOT NULL,
	`status` varchar(50) DEFAULT 'PENDING',
	`prior_art_notes` text,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `patent_assistance_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `permissions` (
	`id` varchar(36) NOT NULL,
	`name` varchar(100) NOT NULL,
	CONSTRAINT `permissions_id` PRIMARY KEY(`id`),
	CONSTRAINT `permissions_name_unique` UNIQUE(`name`)
);
--> statement-breakpoint
CREATE TABLE `property` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`owner_user_id` varchar(36) NOT NULL,
	`title_number` varchar(255) NOT NULL,
	`address` text,
	`area_sq_meters` decimal(10,2),
	`registration_date` date,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `property_id` PRIMARY KEY(`id`),
	CONSTRAINT `property_title_number_unique` UNIQUE(`title_number`)
);
--> statement-breakpoint
CREATE TABLE `role_permissions` (
	`role_id` varchar(36) NOT NULL,
	`permission_id` varchar(36) NOT NULL
);
--> statement-breakpoint
CREATE TABLE `roles` (
	`id` varchar(36) NOT NULL,
	`name` varchar(50) NOT NULL,
	CONSTRAINT `roles_id` PRIMARY KEY(`id`),
	CONSTRAINT `roles_name_unique` UNIQUE(`name`)
);
--> statement-breakpoint
CREATE TABLE `scheme_match` (
	`id` varchar(36) NOT NULL,
	`user_id` varchar(36) NOT NULL,
	`matched_schemes` json NOT NULL DEFAULT ('[]'),
	`criteria` json NOT NULL DEFAULT ('{}'),
	`matched_at` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `scheme_match_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `service_favorites` (
	`id` varchar(36) NOT NULL,
	`user_id` varchar(36) NOT NULL,
	`service_id` varchar(36) NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	CONSTRAINT `service_favorites_id` PRIMARY KEY(`id`),
	CONSTRAINT `service_favorites_user_service_unique` UNIQUE(`user_id`,`service_id`)
);
--> statement-breakpoint
CREATE TABLE `services` (
	`id` varchar(36) NOT NULL,
	`name` varchar(100) NOT NULL,
	`description` text,
	`module_type` varchar(50) NOT NULL,
	`responsible_level_id` varchar(36),
	CONSTRAINT `services_id` PRIMARY KEY(`id`),
	CONSTRAINT `services_name_unique` UNIQUE(`name`)
);
--> statement-breakpoint
CREATE TABLE `student_projects` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`student_user_id` varchar(36) NOT NULL,
	`supervisor_user_id` varchar(36),
	`title` text NOT NULL,
	`description` text,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `student_projects_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `tax_record` (
	`id` varchar(36) NOT NULL,
	`application_id` varchar(36),
	`taxpayer_user_id` varchar(36) NOT NULL,
	`year` int NOT NULL,
	`amount` decimal(10,2),
	`paid` boolean DEFAULT false,
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `tax_record_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `templates` (
	`id` varchar(36) NOT NULL,
	`name` text NOT NULL,
	`description` text,
	`template_content` text NOT NULL,
	`fields` json NOT NULL DEFAULT ('[]'),
	`created_at` timestamp NOT NULL DEFAULT (now()),
	`updated_at` timestamp NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `templates_id` PRIMARY KEY(`id`)
);
--> statement-breakpoint
CREATE TABLE `user_roles` (
	`user_id` varchar(36) NOT NULL,
	`role_id` varchar(36) NOT NULL
);
--> statement-breakpoint
ALTER TABLE `users` MODIFY COLUMN `id` varchar(36) NOT NULL DEFAULT 'uuid_v4()';--> statement-breakpoint
ALTER TABLE `users` MODIFY COLUMN `email` varchar(255);--> statement-breakpoint
ALTER TABLE `users` MODIFY COLUMN `role` enum('citizen','employee','department_admin','system_auditor') NOT NULL DEFAULT 'citizen';--> statement-breakpoint
ALTER TABLE `users` ADD `aadhaar_id` varchar(12);--> statement-breakpoint
ALTER TABLE `users` ADD `pan_id` varchar(10);--> statement-breakpoint
ALTER TABLE `users` ADD `username` varchar(255);--> statement-breakpoint
ALTER TABLE `users` ADD `password_hash` varchar(255);--> statement-breakpoint
ALTER TABLE `users` ADD `phone_number` varchar(20);--> statement-breakpoint
ALTER TABLE `users` ADD `full_name` text;--> statement-breakpoint
ALTER TABLE `users` ADD `date_of_birth` date;--> statement-breakpoint
ALTER TABLE `users` ADD `address` text;--> statement-breakpoint
ALTER TABLE `users` ADD `is_employee` boolean DEFAULT false;--> statement-breakpoint
ALTER TABLE `users` ADD `is_active` boolean DEFAULT true;--> statement-breakpoint
ALTER TABLE `applications` ADD CONSTRAINT `applications_user_id_users_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `applications` ADD CONSTRAINT `applications_service_id_services_id_fk` FOREIGN KEY (`service_id`) REFERENCES `services`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `digital_signatures` ADD CONSTRAINT `digital_signatures_user_id_users_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `modification_requests` ADD CONSTRAINT `modification_requests_user_id_users_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `role_permissions` ADD CONSTRAINT `role_permissions_role_id_roles_id_fk` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `role_permissions` ADD CONSTRAINT `role_permissions_permission_id_permissions_id_fk` FOREIGN KEY (`permission_id`) REFERENCES `permissions`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `service_favorites` ADD CONSTRAINT `service_favorites_user_id_users_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `service_favorites` ADD CONSTRAINT `service_favorites_service_id_services_id_fk` FOREIGN KEY (`service_id`) REFERENCES `services`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `user_roles` ADD CONSTRAINT `user_roles_user_id_users_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE `user_roles` ADD CONSTRAINT `user_roles_role_id_roles_id_fk` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`) ON DELETE cascade ON UPDATE no action;