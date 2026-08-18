import { useAuth } from "@/_core/hooks/useAuth";
import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Loader2, Shield, AlertCircle } from "lucide-react";
import { useState } from "react";

const ACTION_COLORS: Record<string, string> = {
  APPLICATION_SUBMITTED: "bg-blue-600",
  MODIFICATION_REQUEST_INITIATED: "bg-yellow-600",
  MODIFICATION_APPROVED: "bg-green-600",
  MODIFICATION_REJECTED: "bg-red-600",
  USER_LOGIN: "bg-indigo-600",
  USER_LOGOUT: "bg-gray-600",
};

export default function AuditLogViewer() {
  const { user, loading } = useAuth();
  const [offset, setOffset] = useState(0);
  const limit = 50;

  const { data: auditLogs, isLoading } = trpc.audit.list.useQuery(
    { limit, offset },
    { enabled: !!user && user.role === "system_auditor" }
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Loader2 className="animate-spin text-primary" size={48} />
      </div>
    );
  }

  if (!user || user.role !== "system_auditor") {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Card className="w-full max-w-md border-border">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-foreground">
              <AlertCircle size={24} className="text-destructive" />
              Access Denied
            </CardTitle>
            <CardDescription>Only System Auditors can access the audit log viewer.</CardDescription>
          </CardHeader>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Header */}
      <div className="border-b border-border bg-card">
        <div className="container py-8">
          <h1 className="text-4xl font-bold text-foreground mb-2 flex items-center gap-3">
            <Shield size={32} className="text-primary" />
            Immutable Audit Log
          </h1>
          <p className="text-muted-foreground">
            Tamper-evident record of all system actions. This log is append-only and cannot be modified.
          </p>
        </div>
      </div>

      {/* Audit Logs */}
      <div className="container py-12">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="animate-spin text-primary" size={48} />
          </div>
        ) : auditLogs && auditLogs.length > 0 ? (
          <>
            <div className="space-y-4">
              {auditLogs.map((log) => (
                <Card key={log.id} className="border-border hover:shadow-lg transition-shadow">
                  <CardContent className="pt-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {/* Left Column */}
                      <div>
                        <div className="mb-4">
                          <Badge className={`${ACTION_COLORS[log.actionType] || "bg-gray-600"} text-white`}>
                            {log.actionType.replace(/_/g, " ")}
                          </Badge>
                        </div>
                        <div className="space-y-2 text-sm">
                          <p>
                            <span className="text-muted-foreground">Timestamp:</span>
                            <br />
                            <span className="text-foreground font-mono">
                              {new Date(log.eventTimestamp).toLocaleString()}
                            </span>
                          </p>
                          <p>
                            <span className="text-muted-foreground">Actor:</span>
                            <br />
                            <span className="text-foreground font-mono">{log.actorUserId || "System"}</span>
                          </p>
                        </div>
                      </div>

                      {/* Right Column */}
                      <div className="space-y-2 text-sm">
                        {log.targetTable && (
                          <p>
                            <span className="text-muted-foreground">Target Table:</span>
                            <br />
                            <span className="text-foreground font-mono">{log.targetTable}</span>
                          </p>
                        )}
                        {log.targetRecordId && (
                          <p>
                            <span className="text-muted-foreground">Record ID:</span>
                            <br />
                            <span className="text-foreground font-mono">{log.targetRecordId}</span>
                          </p>
                        )}
                        {log.isTamperProof && (
                          <p className="flex items-center gap-2">
                            <Shield size={16} className="text-green-600" />
                            <span className="text-green-600 font-semibold">Tamper-Proof</span>
                          </p>
                        )}
                      </div>
                    </div>

                    {/* Changed Data */}
                    {log.changedData ? (
                      <div className="mt-4 pt-4 border-t border-border">
                        <p className="text-xs text-muted-foreground mb-2">Changed Data:</p>
                        <pre className="bg-card border border-border rounded p-3 text-xs overflow-auto max-h-32 text-foreground">
                          {String(log.changedData)}
                        </pre>
                      </div>
                    ) : null}
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Pagination */}
            <div className="flex items-center justify-between mt-8 pt-8 border-t border-border">
              <p className="text-sm text-muted-foreground">
                Showing {offset + 1} to {offset + auditLogs.length} of audit logs
              </p>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  className="border-border text-foreground hover:bg-accent"
                  onClick={() => setOffset(Math.max(0, offset - limit))}
                  disabled={offset === 0}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  className="border-border text-foreground hover:bg-accent"
                  onClick={() => setOffset(offset + limit)}
                  disabled={auditLogs.length < limit}
                >
                  Next
                </Button>
              </div>
            </div>
          </>
        ) : (
          <Card className="border-border">
            <CardContent className="pt-6 text-center">
              <p className="text-muted-foreground">No audit logs found.</p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
