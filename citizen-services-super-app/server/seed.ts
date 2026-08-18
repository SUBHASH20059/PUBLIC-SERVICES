import { getDb } from "./db";
import { services, administrativeLevels } from "../drizzle/schema";

export const ADMIN_LEVELS = [
  { id: "lvl-regional", name: "Regional Level", parentLevelId: null },
  { id: "lvl-district", name: "District Level", parentLevelId: "lvl-regional" },
  { id: "lvl-state", name: "State Level", parentLevelId: "lvl-district" },
  { id: "lvl-central", name: "Central Level", parentLevelId: "lvl-state" },
  { id: "lvl-national", name: "National / Apex Level", parentLevelId: "lvl-central" },
] as const;

type ServiceSeed = {
  id: string;
  name: string;
  description: string;
  moduleType: string;
  responsibleLevelId: string;
};

/**
 * Broad catalog of high-demand government-facing services.
 * State-specific or external services are represented as routed catalog entries;
 * the UI must not imply that this app itself is the issuing authority.
 */
export const SERVICE_CATALOG: ServiceSeed[] = [
  // Identity, cards, and digital records
  { id: "srv-aadhaar", name: "Aadhaar Enrolment & Updates", description: "Find an official enrolment or demographic-update route for Aadhaar services.", moduleType: "identity_cards", responsibleLevelId: "lvl-national" },
  { id: "srv-pan", name: "PAN Card Processing", description: "Permanent Account Number application, correction, and reprint routing.", moduleType: "identity_cards", responsibleLevelId: "lvl-central" },
  { id: "srv-voter", name: "Voter ID / Electoral Services", description: "New voter registration, corrections, transfers, and electoral-roll services.", moduleType: "identity_cards", responsibleLevelId: "lvl-national" },
  { id: "srv-passport", name: "Passport Application & Renewal", description: "Passport application, renewal, appointment, and status-routing support.", moduleType: "identity_cards", responsibleLevelId: "lvl-central" },
  { id: "srv-eshram", name: "e-Shram Worker Registration", description: "Registration and profile services for eligible unorganised workers.", moduleType: "identity_cards", responsibleLevelId: "lvl-central" },
  { id: "srv-ayushman", name: "Ayushman Bharat Health Card", description: "Health-beneficiary card discovery and eligibility routing.", moduleType: "identity_cards", responsibleLevelId: "lvl-central" },
  { id: "srv-abha", name: "ABHA Health ID", description: "Digital health identity creation and account-support routing.", moduleType: "identity_cards", responsibleLevelId: "lvl-central" },
  { id: "srv-disability-id", name: "Disability Certificate & UDID", description: "Disability certification and UDID-related service routing.", moduleType: "identity_cards", responsibleLevelId: "lvl-central" },
  { id: "srv-ration-card", name: "Ration Card Services", description: "New ration-card applications, corrections, and household-member updates.", moduleType: "identity_cards", responsibleLevelId: "lvl-state" },
  { id: "srv-digilocker", name: "DigiLocker Document Access", description: "Access and verify government-issued digital records through an official repository.", moduleType: "identity_cards", responsibleLevelId: "lvl-central" },

  // Civil registration and certificates
  { id: "srv-marriage", name: "Marriage Registration", description: "Official civil marriage registration and certificate issuance.", moduleType: "civil_certificates", responsibleLevelId: "lvl-district" },
  { id: "srv-birth", name: "Birth Certificate", description: "Birth registration, certificate issuance, and correction routing.", moduleType: "civil_certificates", responsibleLevelId: "lvl-regional" },
  { id: "srv-death", name: "Death Certificate", description: "Death registration, certificate issuance, and correction routing.", moduleType: "civil_certificates", responsibleLevelId: "lvl-regional" },
  { id: "srv-domicile", name: "Domicile / Residence Certificate", description: "Resident and domicile certificate application support.", moduleType: "civil_certificates", responsibleLevelId: "lvl-district" },
  { id: "srv-income", name: "Income Certificate", description: "Income certificate application and verification routing.", moduleType: "civil_certificates", responsibleLevelId: "lvl-district" },
  { id: "srv-caste", name: "Caste / Community Certificate", description: "Caste or community certificate application and verification routing.", moduleType: "civil_certificates", responsibleLevelId: "lvl-district" },
  { id: "srv-ews", name: "EWS Certificate", description: "Economically Weaker Section certificate routing where applicable.", moduleType: "civil_certificates", responsibleLevelId: "lvl-district" },
  { id: "srv-legal-heir", name: "Legal Heir Certificate", description: "Legal-heir or surviving-member certificate routing.", moduleType: "civil_certificates", responsibleLevelId: "lvl-district" },
  { id: "srv-character", name: "Character Certificate", description: "Character or police-verification certificate routing where available.", moduleType: "civil_certificates", responsibleLevelId: "lvl-district" },
  { id: "srv-family", name: "Family / Resident Register", description: "Household and resident-register services provided by local authorities.", moduleType: "civil_certificates", responsibleLevelId: "lvl-regional" },

  // Transport and mobility
  { id: "srv-dl", name: "Driving License Issuance", description: "New driving-license applications, tests, and renewals.", moduleType: "transport", responsibleLevelId: "lvl-district" },
  { id: "srv-rc", name: "Vehicle Registration Certificate", description: "Vehicle registration, transfer, and registration-certificate services.", moduleType: "transport", responsibleLevelId: "lvl-state" },
  { id: "srv-vehicle-tax", name: "Motor Vehicle Tax", description: "Vehicle-tax payment and status-routing support.", moduleType: "transport", responsibleLevelId: "lvl-state" },
  { id: "srv-permit", name: "Transport Permit Services", description: "Commercial vehicle permits and related transport-authority services.", moduleType: "transport", responsibleLevelId: "lvl-state" },
  { id: "srv-challan", name: "Traffic Challan Status & Payment", description: "Traffic-violation status lookup and official payment routing.", moduleType: "transport", responsibleLevelId: "lvl-state" },
  { id: "srv-puc", name: "Vehicle Fitness / PUC Routing", description: "Vehicle fitness and pollution-certificate service discovery.", moduleType: "transport", responsibleLevelId: "lvl-state" },

  // Land, property, and legal
  { id: "srv-land", name: "Land Records / Record of Rights", description: "Land-record, ownership, and record-of-rights service discovery.", moduleType: "land_legal", responsibleLevelId: "lvl-regional" },
  { id: "srv-mutation", name: "Land Mutation", description: "Ownership-change and land-mutation application routing.", moduleType: "land_legal", responsibleLevelId: "lvl-district" },
  { id: "srv-encumbrance", name: "Encumbrance Certificate", description: "Property encumbrance-certificate application routing.", moduleType: "land_legal", responsibleLevelId: "lvl-district" },
  { id: "srv-deed", name: "Deed Registration & Tracking", description: "Property deed execution, registration, and status tracking.", moduleType: "land_legal", responsibleLevelId: "lvl-district" },
  { id: "srv-property-tax", name: "Property Tax Services", description: "Property-tax assessment, payment, and receipt routing.", moduleType: "land_legal", responsibleLevelId: "lvl-regional" },
  { id: "srv-building", name: "Building Plan Approval", description: "Local building-plan approval and permit-routing services.", moduleType: "land_legal", responsibleLevelId: "lvl-regional" },
  { id: "srv-legal-agreement", name: "Legal Agreement Execution", description: "Digitally supported legal-agreement and deed workflow entry point.", moduleType: "land_legal", responsibleLevelId: "lvl-district" },

  // Business, tax, and startup
  { id: "srv-startup", name: "Startup Registration & Recognition", description: "Startup registration and official recognition service routing.", moduleType: "business_tax", responsibleLevelId: "lvl-state" },
  { id: "srv-udyam", name: "Udyam / MSME Registration", description: "Micro, small, and medium enterprise registration routing.", moduleType: "business_tax", responsibleLevelId: "lvl-central" },
  { id: "srv-gst", name: "GST Registration & Returns", description: "Goods and Services Tax registration, return, and compliance routing.", moduleType: "business_tax", responsibleLevelId: "lvl-central" },
  { id: "srv-fssai", name: "Food Business License", description: "Food-business registration and licensing service routing.", moduleType: "business_tax", responsibleLevelId: "lvl-central" },
  { id: "srv-shop", name: "Shop & Establishment Registration", description: "Shop and establishment registration through the relevant authority.", moduleType: "business_tax", responsibleLevelId: "lvl-state" },
  { id: "srv-labour", name: "Labour / Factory License", description: "Labour, factory, and workplace registration routing.", moduleType: "business_tax", responsibleLevelId: "lvl-state" },
  { id: "srv-compliance", name: "Business Compliance Tracking", description: "Compliance calendar, filing reminders, and official-route discovery.", moduleType: "business_tax", responsibleLevelId: "lvl-state" },
  { id: "srv-protection", name: "Startup Protection Framework", description: "Legal, regulatory, and insurance-support information for startups.", moduleType: "business_tax", responsibleLevelId: "lvl-central" },
  { id: "srv-income-tax", name: "Income Tax Services", description: "Income-tax return, refund, notice, and account-service routing.", moduleType: "business_tax", responsibleLevelId: "lvl-central" },
  { id: "srv-epfo", name: "EPFO / UAN Services", description: "Provident-fund account, UAN, and claim-service routing.", moduleType: "business_tax", responsibleLevelId: "lvl-central" },

  // Welfare, social protection, and housing
  { id: "srv-pension", name: "Social Security Pension", description: "Old-age, widow, disability, and other pension-scheme routing.", moduleType: "welfare", responsibleLevelId: "lvl-state" },
  { id: "srv-scholarship", name: "Government Scholarships", description: "Scholarship discovery, eligibility, and application routing.", moduleType: "welfare", responsibleLevelId: "lvl-central" },
  { id: "srv-pm-kisan", name: "PM-KISAN Services", description: "Farmer-benefit registration, status, and payment-support routing.", moduleType: "welfare", responsibleLevelId: "lvl-central" },
  { id: "srv-housing", name: "Government Housing Schemes", description: "Affordable housing and beneficiary-service discovery.", moduleType: "welfare", responsibleLevelId: "lvl-state" },
  { id: "srv-lpg", name: "LPG / Cooking Fuel Subsidy", description: "Cooking-fuel connection and subsidy-service routing.", moduleType: "welfare", responsibleLevelId: "lvl-central" },
  { id: "srv-mgnrega", name: "Rural Employment Services", description: "Rural employment registration and work-demand routing.", moduleType: "welfare", responsibleLevelId: "lvl-regional" },
  { id: "srv-food-security", name: "Food Security & Ration Benefits", description: "Food-security entitlement, portability, and grievance routing.", moduleType: "welfare", responsibleLevelId: "lvl-state" },

  // Education and skills
  { id: "srv-school-admission", name: "School Admission Services", description: "Public-school admission and local education-service routing.", moduleType: "education_skills", responsibleLevelId: "lvl-regional" },
  { id: "srv-exam", name: "Government Examination Services", description: "Exam registration, admit cards, results, and certificate routing.", moduleType: "education_skills", responsibleLevelId: "lvl-state" },
  { id: "srv-education-certificate", name: "Education Certificate Verification", description: "Academic-certificate verification and record-service routing.", moduleType: "education_skills", responsibleLevelId: "lvl-state" },
  { id: "srv-skill", name: "Skill Development Registration", description: "Skill-training, assessment, and certification-service discovery.", moduleType: "education_skills", responsibleLevelId: "lvl-central" },
  { id: "srv-student-scholarship", name: "Student Scholarship Portal", description: "Student scholarship application and status routing.", moduleType: "education_skills", responsibleLevelId: "lvl-central" },

  // Health and agriculture
  { id: "srv-health-appointment", name: "Public Hospital Appointment", description: "Public-health facility appointment and service discovery.", moduleType: "health_agriculture", responsibleLevelId: "lvl-state" },
  { id: "srv-immunization", name: "Immunization Records", description: "Vaccination and immunization-record service routing.", moduleType: "health_agriculture", responsibleLevelId: "lvl-central" },
  { id: "srv-health-insurance", name: "Government Health Insurance", description: "Health-insurance beneficiary and claim-service routing.", moduleType: "health_agriculture", responsibleLevelId: "lvl-central" },
  { id: "srv-soil-health", name: "Soil Health & Farm Advisory", description: "Soil-health card and agriculture-advisory service routing.", moduleType: "health_agriculture", responsibleLevelId: "lvl-central" },
  { id: "srv-crop-insurance", name: "Crop Insurance Services", description: "Crop-insurance registration, claim, and status routing.", moduleType: "health_agriculture", responsibleLevelId: "lvl-central" },
  { id: "srv-market", name: "Agricultural Market Services", description: "Market-price and agricultural-trade service discovery.", moduleType: "health_agriculture", responsibleLevelId: "lvl-state" },

  // Utilities, employment, and public accountability
  { id: "srv-electricity", name: "Electricity Connection & Bill Services", description: "Electricity connection, billing, and complaint routing.", moduleType: "utilities_employment", responsibleLevelId: "lvl-state" },
  { id: "srv-water", name: "Water & Sewerage Services", description: "Local water connection, billing, and complaint routing.", moduleType: "utilities_employment", responsibleLevelId: "lvl-regional" },
  { id: "srv-employment", name: "National Employment Services", description: "Employment registration, job matching, and career-service routing.", moduleType: "utilities_employment", responsibleLevelId: "lvl-central" },
  { id: "srv-police-clearance", name: "Police Clearance Certificate", description: "Police-clearance and verification-service routing.", moduleType: "utilities_employment", responsibleLevelId: "lvl-state" },
  { id: "srv-grievance", name: "Public Grievance / CPGRAMS Routing", description: "Submit and track public-service grievances through the responsible authority.", moduleType: "grievance_justice", responsibleLevelId: "lvl-central" },
  { id: "srv-rti", name: "Right to Information Support", description: "RTI guidance and application-route discovery.", moduleType: "grievance_justice", responsibleLevelId: "lvl-central" },
  { id: "srv-legal-aid", name: "Legal Aid & Lok Adalat Support", description: "Legal-aid and dispute-resolution service discovery.", moduleType: "grievance_justice", responsibleLevelId: "lvl-state" },
  { id: "srv-emergency", name: "Emergency Helplines & Alerts", description: "Verified emergency contact and public-safety information routing.", moduleType: "grievance_justice", responsibleLevelId: "lvl-national" },
];

export async function seedDatabase() {
  const db = await getDb();
  if (!db) {
    console.warn("[Seed] Database not available");
    return;
  }

  try {
    console.log("[Seed] Seeding administrative levels and expanded service catalog...");

    for (const lvl of ADMIN_LEVELS) {
      await db.insert(administrativeLevels).values(lvl).onDuplicateKeyUpdate({ set: { name: lvl.name } });
    }

    for (const srv of SERVICE_CATALOG) {
      await db.insert(services).values(srv).onDuplicateKeyUpdate({
        set: {
          name: srv.name,
          description: srv.description,
          moduleType: srv.moduleType,
          responsibleLevelId: srv.responsibleLevelId,
        },
      });
    }

    console.log(`[Seed] Seeded ${SERVICE_CATALOG.length} services across ${ADMIN_LEVELS.length} administrative levels.`);
  } catch (error) {
    console.error("[Seed] Error seeding database:", error);
  }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  seedDatabase();
}
        
        
