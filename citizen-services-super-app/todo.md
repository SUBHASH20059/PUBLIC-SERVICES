# Project TODO - Citizen Services Super App

## 1. Architecture & Brand Shell
- [x] Transcribe handwritten design notes into a formal Software Design Specification document
- [x] Enhance /architecture page with collapsible sections and interactive layer diagram
- [x] Plan architecture, database schema, and 5-tier hierarchy
- [x] Design dark government-grade theme tokens (navy/indigo, high contrast)
- [ ] Implement distinct civic brand header and responsive navigation shell across all routes

## 2. Security, RBAC & Immutable Audit Logs
- [x] Define user roles: citizen, employee, department_admin, system_auditor
- [ ] Enforce strict RBAC middleware preventing employees from directly writing/modifying records
- [ ] Implement cryptographically tamper-evident, append-only audit logging (no edit/delete endpoints)
- [ ] Scope data visibility strictly to the actor's administrative level (Regional → National)

## 3. Core Services & Citizen Workflows
- [x] Civil, Land/Legal, and Business/Startup service modules
- [x] Expanded Indian Government Cards & Services Catalog (Identity, Welfare, Tax, Health, Education, Transport, Utilities, Grievances)
  - [x] Seed 69 catalog services across 10 service families and 5 administrative levels
  - [x] Add searchable/filterable catalog page
  - [x] Add generic citizen application entry route for every catalog item
- [x] Application status tracking with real-time state updates
- [x] Secure Service Favorites & Bookmarking (Database-backed per-user persistence and quick-access view)
  - [x] Per-user unique database constraint and protected tRPC procedures
  - [x] Bookmark toggle controls on service cards
  - [x] Dedicated /favorites quick-access route and saved-only filter
  - [x] Authorization tests and visual verification
- [ ] Secure document vault with S3 storage-backed uploads and owner-only retrieval
- [ ] Citizen-initiated modification workflow with digital signature & verification steps

## 4. Employee & Auditor Workflows
- [x] Employee case queue interface for reviewing pending modifications
- [ ] Decision logging, proposed actions, and additional information requests
- [x] Immutable audit log viewer for system auditors

## 5. RAG-Style AI Assistant & Testing
- [ ] Integrate RAG-style AI assistant grounded in official scheme documents
- [ ] Write robust vitest suites covering RBAC boundaries, workflows, and audit immutability
- [x] Take preview screenshots and verify UI rendering
