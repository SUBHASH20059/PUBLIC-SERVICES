import { describe, expect, it } from "vitest";
import { appRouter } from "./routers";
import type { TrpcContext } from "./_core/context";

function createUnauthenticatedContext(): TrpcContext {
  return {
    user: null,
    req: { protocol: "https", headers: {} } as TrpcContext["req"],
    res: {} as TrpcContext["res"],
  };
}

describe("service favorites authorization", () => {
  it("rejects an unauthenticated favorites list request", async () => {
    const caller = appRouter.createCaller(createUnauthenticatedContext());

    await expect(caller.favorites.list()).rejects.toMatchObject({ code: "UNAUTHORIZED" });
  });

  it("rejects an unauthenticated favorite toggle request", async () => {
    const caller = appRouter.createCaller(createUnauthenticatedContext());

    await expect(caller.favorites.toggle({ serviceId: "srv-pan" })).rejects.toMatchObject({ code: "UNAUTHORIZED" });
  });
});
        
        
