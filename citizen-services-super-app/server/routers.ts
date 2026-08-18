import { COOKIE_NAME } from "@shared/const";
import { getSessionCookieOptions } from "./_core/cookies";
import { systemRouter } from "./_core/systemRouter";
import { publicProcedure, router, protectedProcedure, adminProcedure } from "./_core/trpc";
import { TRPCError } from "@trpc/server";
import { z } from "zod";
import { getDb } from "./db";
import { applications, services, serviceFavorites, modificationRequests, auditLogs, users } from "../drizzle/schema";
import { eq, and } from "drizzle-orm";

/**
 * RBAC Middleware: Verify user role and administrative level
 */
const requireRole = (allowedRoles: string[]) => {
  return protectedProcedure.use(async ({ ctx, next }) => {
    if (!allowedRoles.includes(ctx.user.role)) {
      throw new TRPCError({
        code: "FORBIDDEN",
        message: `Access denied. Required roles: ${allowedRoles.join(", ")}`,
      });
    }
    return next({ ctx });
  });
};

/**
 * Services Router: List available government services by track
 */
const servicesRouter = router({
  list: publicProcedure
    .input(z.object({ moduleType: z.string().optional() }).optional())
    .query(async ({ input }) => {
      const db = await getDb();
      if (!db) return [];
      
      try {
        const query = input?.moduleType
          ? db.select().from(services).where(eq(services.moduleType, input.moduleType))
          : db.select().from(services);
        
        return await query;
      } catch (error) {
        console.error("[Services] Error listing services:", error);
        return [];
      }
    }),

  getById: publicProcedure
    .input(z.string())
    .query(async ({ input }) => {
      const db = await getDb();
      if (!db) return null;
      
      try {
        const result = await db.select().from(services).where(eq(services.id, input)).limit(1);
        return result[0] || null;
      } catch (error) {
        console.error("[Services] Error getting service:", error);
        return null;
      }
    }),
});

/**
 * Service Favorites Router: authenticated, user-scoped bookmarks.
 */
const favoritesRouter = router({
  list: protectedProcedure.query(async ({ ctx }) => {
    const db = await getDb();
    if (!db) return [];

    try {
      return await db
        .select({
          id: serviceFavorites.id,
          serviceId: services.id,
          name: services.name,
          description: services.description,
          moduleType: services.moduleType,
          responsibleLevelId: services.responsibleLevelId,
          createdAt: serviceFavorites.createdAt,
        })
        .from(serviceFavorites)
        .innerJoin(services, eq(serviceFavorites.serviceId, services.id))
        .where(eq(serviceFavorites.userId, ctx.user.id));
    } catch (error) {
      console.error("[Favorites] Error listing favorites:", error);
      return [];
    }
  }),

  toggle: protectedProcedure
    .input(z.object({ serviceId: z.string().min(1) }))
    .mutation(async ({ ctx, input }) => {
      const db = await getDb();
      if (!db) throw new TRPCError({ code: "INTERNAL_SERVER_ERROR", message: "Database unavailable" });

      const service = await db.select({ id: services.id }).from(services).where(eq(services.id, input.serviceId)).limit(1);
      if (!service[0]) throw new TRPCError({ code: "NOT_FOUND", message: "Government service not found" });

      try {
        const existing = await db
          .select({ id: serviceFavorites.id })
          .from(serviceFavorites)
          .where(and(eq(serviceFavorites.userId, ctx.user.id), eq(serviceFavorites.serviceId, input.serviceId)))
          .limit(1);

        if (existing[0]) {
          await db.delete(serviceFavorites).where(eq(serviceFavorites.id, existing[0].id));
          await db.insert(auditLogs).values({
            eventTimestamp: new Date(),
            actorUserId: ctx.user.id,
            actionType: "SERVICE_FAVORITE_REMOVED",
            targetTable: "service_favorites",
            targetRecordId: existing[0].id,
            changedData: JSON.stringify({ serviceId: input.serviceId }),
            isTamperProof: true,
          });
          return { isFavorite: false } as const;
        }

        const favoriteId = crypto.randomUUID();
        await db.insert(serviceFavorites).values({
          id: favoriteId,
          userId: ctx.user.id,
          serviceId: input.serviceId,
          createdAt: new Date(),
        });
        await db.insert(auditLogs).values({
          eventTimestamp: new Date(),
          actorUserId: ctx.user.id,
          actionType: "SERVICE_FAVORITE_ADDED",
          targetTable: "service_favorites",
          targetRecordId: favoriteId,
          changedData: JSON.stringify({ serviceId: input.serviceId }),
          isTamperProof: true,
        });
        return { isFavorite: true } as const;
      } catch (error) {
        console.error("[Favorites] Error toggling favorite:", error);
        throw new TRPCError({ code: "INTERNAL_SERVER_ERROR", message: "Unable to update favorite" });
      }
    }),
});

/**
 * Applications Router: Citizen applications and status tracking
 */
const applicationsRouter = router({
  submit: protectedProcedure
    .input(z.object({
      serviceId: z.string(),
      applicationData: z.record(z.string(), z.any()),
    }))
    .mutation(async ({ ctx, input }) => {
      if (ctx.user.role !== "citizen") {
        throw new TRPCError({
          code: "FORBIDDEN",
          message: "Only citizens can submit applications",
        });
      }

      const db = await getDb();
      if (!db) throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });

      try {
        await db.insert(applications).values({
          id: crypto.randomUUID(),
          userId: ctx.user.id,
          serviceId: input.serviceId,
          status: "SUBMITTED",
          applicationData: input.applicationData,
          submissionDate: new Date(),
          lastUpdatedAt: new Date(),
        });

        // Log audit event
        await db.insert(auditLogs).values({
          eventTimestamp: new Date(),
          actorUserId: ctx.user.id,
          actionType: "APPLICATION_SUBMITTED",
          targetTable: "applications",
          changedData: JSON.stringify({ serviceId: input.serviceId }),
          isTamperProof: true,
        });

        return { success: true, message: "Application submitted successfully" };
      } catch (error) {
        console.error("[Applications] Error submitting application:", error);
        throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });
      }
    }),

  listMyCitizen: protectedProcedure
    .query(async ({ ctx }) => {
      if (ctx.user.role !== "citizen") {
        throw new TRPCError({
          code: "FORBIDDEN",
          message: "Only citizens can view their applications",
        });
      }

      const db = await getDb();
      if (!db) return [];

      try {
        return await db.select().from(applications).where(eq(applications.userId, ctx.user.id));
      } catch (error) {
        console.error("[Applications] Error listing citizen applications:", error);
        return [];
      }
    }),

  getById: protectedProcedure
    .input(z.string())
    .query(async ({ ctx, input }) => {
      const db = await getDb();
      if (!db) return null;

      try {
        const result = await db.select().from(applications).where(eq(applications.id, input)).limit(1);
        const app = result[0];

        if (!app) return null;

        // Citizens can only view their own applications
        if (ctx.user.role === "citizen" && app.userId !== ctx.user.id) {
          throw new TRPCError({ code: "FORBIDDEN" });
        }

        return app;
      } catch (error) {
        console.error("[Applications] Error getting application:", error);
        return null;
      }
    }),
});

/**
 * Modification Requests Router: Secure citizen-initiated data modifications
 */
const modificationRouter = router({
  requestModification: protectedProcedure
    .input(z.object({
      targetTable: z.string(),
      targetRecordId: z.string(),
      fieldName: z.string(),
      newValue: z.string(),
      oldValue: z.string().optional(),
    }))
    .mutation(async ({ ctx, input }) => {
      // Only citizens can request modifications of their own records
      if (ctx.user.role !== "citizen") {
        throw new TRPCError({
          code: "FORBIDDEN",
          message: "Only citizens can request modifications",
        });
      }

      const db = await getDb();
      if (!db) throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });

      try {
        await db.insert(modificationRequests).values({
          id: crypto.randomUUID(),
          userId: ctx.user.id,
          targetTable: input.targetTable,
          targetRecordId: input.targetRecordId,
          fieldName: input.fieldName,
          oldValue: input.oldValue || null,
          newValue: input.newValue,
          requestStatus: "PENDING",
          initiatedAt: new Date(),
          verificationDetails: JSON.stringify({
            signatureRequired: true,
            verificationMethod: "aadhaar_otp",
          }),
        });

        // Log audit event
        await db.insert(auditLogs).values({
          eventTimestamp: new Date(),
          actorUserId: ctx.user.id,
          actionType: "MODIFICATION_REQUEST_INITIATED",
          targetTable: input.targetTable,
          targetRecordId: input.targetRecordId,
          changedData: JSON.stringify({ fieldName: input.fieldName, newValue: input.newValue }),
          isTamperProof: true,
        });

        return { success: true, message: "Modification request submitted" };
      } catch (error) {
        console.error("[Modifications] Error requesting modification:", error);
        throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });
      }
    }),

  listPending: requireRole(["department_admin", "system_auditor"])
    .query(async ({ ctx }) => {
      const db = await getDb();
      if (!db) return [];

      try {
        return await db.select().from(modificationRequests).where(eq(modificationRequests.requestStatus, "PENDING"));
      } catch (error) {
        console.error("[Modifications] Error listing pending requests:", error);
        return [];
      }
    }),

  approveModification: requireRole(["department_admin", "system_auditor"])
    .input(z.string())
    .mutation(async ({ ctx, input }) => {
      const db = await getDb();
      if (!db) throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });

      try {
        // Update modification request status
        await db.update(modificationRequests)
          .set({
            requestStatus: "APPROVED",
            approvedByUserId: ctx.user.id,
            approvedAt: new Date(),
          })
          .where(eq(modificationRequests.id, input));

        // Log audit event
        await db.insert(auditLogs).values({
          eventTimestamp: new Date(),
          actorUserId: ctx.user.id,
          actionType: "MODIFICATION_APPROVED",
          targetTable: "modification_requests",
          targetRecordId: input,
          changedData: null,
          isTamperProof: true,
        });

        return { success: true, message: "Modification approved" };
      } catch (error) {
        console.error("[Modifications] Error approving modification:", error);
        throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });
      }
    }),

  rejectModification: requireRole(["department_admin", "system_auditor"])
    .input(z.object({ id: z.string(), reason: z.string() }))
    .mutation(async ({ ctx, input }) => {
      const db = await getDb();
      if (!db) throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });

      try {
        await db.update(modificationRequests)
          .set({
            requestStatus: "REJECTED",
            approvedByUserId: ctx.user.id,
            approvedAt: new Date(),
          })
          .where(eq(modificationRequests.id, input.id));

        // Log audit event
        await db.insert(auditLogs).values({
          eventTimestamp: new Date(),
          actorUserId: ctx.user.id,
          actionType: "MODIFICATION_REJECTED",
          targetTable: "modification_requests",
          targetRecordId: input.id,
          changedData: JSON.stringify({ reason: input.reason }),
          isTamperProof: true,
        });

        return { success: true, message: "Modification rejected" };
      } catch (error) {
        console.error("[Modifications] Error rejecting modification:", error);
        throw new TRPCError({ code: "INTERNAL_SERVER_ERROR" });
      }
    }),
});

/**
 * Audit Log Router: Immutable audit trail (System Auditors only)
 */
const auditRouter = router({
  list: requireRole(["system_auditor"])
    .input(z.object({
      limit: z.number().default(50),
      offset: z.number().default(0),
    }).optional())
    .query(async ({ input }) => {
      const db = await getDb();
      if (!db) return [];

      try {
        const limit = input?.limit || 50;
        const offset = input?.offset || 0;
        return await db.select().from(auditLogs).limit(limit).offset(offset);
      } catch (error) {
        console.error("[Audit] Error listing audit logs:", error);
        return [];
      }
    }),

  getByActor: requireRole(["system_auditor"])
    .input(z.string())
    .query(async ({ input }) => {
      const db = await getDb();
      if (!db) return [];

      try {
        return await db.select().from(auditLogs).where(eq(auditLogs.actorUserId, input));
      } catch (error) {
        console.error("[Audit] Error getting actor audit logs:", error);
        return [];
      }
    }),

  getByAction: requireRole(["system_auditor"])
    .input(z.string())
    .query(async ({ input }) => {
      const db = await getDb();
      if (!db) return [];

      try {
        return await db.select().from(auditLogs).where(eq(auditLogs.actionType, input));
      } catch (error) {
        console.error("[Audit] Error getting action audit logs:", error);
        return [];
      }
    }),
});

/**
 * Main App Router
 */
export const appRouter = router({
  system: systemRouter,
  auth: router({
    me: publicProcedure.query(opts => opts.ctx.user),
    logout: publicProcedure.mutation(({ ctx }) => {
      const cookieOptions = getSessionCookieOptions(ctx.req);
      ctx.res.clearCookie(COOKIE_NAME, { ...cookieOptions, maxAge: -1 });
      return {
        success: true,
      } as const;
    }),
  }),
  services: servicesRouter,
  favorites: favoritesRouter,
  applications: applicationsRouter,
  modifications: modificationRouter,
  audit: auditRouter,
});

export type AppRouter = typeof appRouter;
