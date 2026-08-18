import { useAuth } from "@/_core/hooks/useAuth";
import { trpc } from "@/lib/trpc";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Loader2, AlertCircle, CheckCircle, Clock } from "lucide-react";
import { useState } from "react";

const STATUS_COLORS: Record<string, string> = {
  PENDING: "bg-yellow-600",
  APPROVED: "bg-green-600",
  REJECTED: "bg-red-600",
  IN_REVIEW: "bg-blue-600",
};

export default function EmployeeCaseQueue() {
  const { user, loading } = useAuth();
  const [selectedCase, setSelectedCase] = useState<string | null>(null);
  const [action, setAction] = useState<"approve" | "reject" | null>(null);

  const { data: pendingMods, isLoading } = trpc.modifications.listPending.useQuery(
    undefined,
    { enabled: !!user && (user.role === "employee" || user.role === "department_admin") }
  );

  const approveMutation = trpc.modifications.approveModification.useMutation();
  const rejectMutation = trpc.modifications.rejectModification.useMutation();

  const handleApprove = async (modId: string) => {
    try {
      await approveMutation.mutateAsync(modId);
      setSelectedCase(null);
      setAction(null);
    } catch (error) {
      console.error("Error approving modification:", error);
    }
  };

  const handleReject = async (modId: string) => {
    try {
      await rejectMutation.mutateAsync({ id: modId, reason: "Rejected by case worker" });
      setSelectedCase(null);
      setAction(null);
    } catch (error) {
      console.error("Error rejecting modification:", error);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Loader2 className="animate-spin text-primary" size={48} />
      </div>
    );
  }

  if (!user || (user.role !== "employee" && user.role !== "department_admin")) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Card className="w-full max-w-md border-border">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-foreground">
              <AlertCircle size={24} className="text-destructive" />
              Access Denied
            </CardTitle>
            <CardDescription>Only employees and department admins can access the case queue.</CardDescription>
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
          <h1 className="text-4xl font-bold text-foreground mb-2">Case Queue</h1>
          <p className="text-muted-foreground">
            Review and process pending modification requests. All actions are logged and audited.
          </p>
        </div>
      </div>

      {/* Cases */}
      <div className="container py-12">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="animate-spin text-primary" size={48} />
          </div>
        ) : pendingMods && pendingMods.length > 0 ? (
          <div className="space-y-6">
            {pendingMods.map((mod) => (
              <Card
                key={mod.id}
                className={`border-border cursor-pointer transition-all ${
                  selectedCase === mod.id ? "ring-2 ring-primary" : "hover:shadow-lg"
                }`}
                onClick={() => setSelectedCase(selectedCase === mod.id ? null : mod.id)}
              >
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div>
                      <CardTitle className="text-lg text-foreground">
                        Modification Request #{mod.id.slice(0, 8)}
                      </CardTitle>
                      <CardDescription className="text-sm text-muted-foreground">
                        Submitted: {new Date(mod.initiatedAt).toLocaleDateString()}
                      </CardDescription>
                    </div>
                    <Badge className={`${STATUS_COLORS[mod.requestStatus] || "bg-gray-600"} text-white`}>
                      {mod.requestStatus}
                    </Badge>
                  </div>
                </CardHeader>

                {selectedCase === mod.id && (
                  <CardContent className="space-y-4">
                    {/* Modification Details */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 bg-card border border-border rounded">
                      <div>
                        <p className="text-xs text-muted-foreground mb-1">Target Table</p>
                        <p className="text-foreground font-mono">{mod.targetTable}</p>
                      </div>
                      <div>
                        <p className="text-xs text-muted-foreground mb-1">Field Name</p>
                        <p className="text-foreground font-mono">{mod.fieldName}</p>
                      </div>
                      <div className="md:col-span-2">
                        <p className="text-xs text-muted-foreground mb-1">Old Value</p>
                        <p className="text-foreground font-mono break-words">{mod.oldValue || "N/A"}</p>
                      </div>
                      <div className="md:col-span-2">
                        <p className="text-xs text-muted-foreground mb-1">New Value</p>
                        <p className="text-foreground font-mono break-words">{mod.newValue}</p>
                      </div>
                    </div>

                    {/* Verification Details */}
                    {mod.verificationDetails ? (
                      <div className="p-4 bg-card border border-border rounded">
                        <p className="text-xs text-muted-foreground mb-2">Verification Details</p>
                        <pre className="text-xs text-foreground overflow-auto">
                          {String(mod.verificationDetails)}
                        </pre>
                      </div>
                    ) : null}

                    {/* Action Buttons */}
                    {mod.requestStatus === "PENDING" && (
                      <div className="flex gap-3 pt-4 border-t border-border">
                        <Button
                          className="flex-1 bg-green-600 hover:bg-green-700 text-white"
                          onClick={() => handleApprove(mod.id)}
                          disabled={approveMutation.isPending}
                        >
                          {approveMutation.isPending ? (
                            <>
                              <Loader2 className="mr-2 animate-spin" size={16} />
                              Approving...
                            </>
                          ) : (
                            <>
                              <CheckCircle className="mr-2" size={16} />
                              Approve
                            </>
                          )}
                        </Button>
                        <Button
                          variant="outline"
                          className="flex-1 border-destructive text-destructive hover:bg-destructive hover:text-white"
                          onClick={() => handleReject(mod.id)}
                          disabled={rejectMutation.isPending}
                        >
                          {rejectMutation.isPending ? (
                            <>
                              <Loader2 className="mr-2 animate-spin" size={16} />
                              Rejecting...
                            </>
                          ) : (
                            <>
                              <AlertCircle className="mr-2" size={16} />
                              Reject
                            </>
                          )}
                        </Button>
                      </div>
                    )}
                  </CardContent>
                )}
              </Card>
            ))}
          </div>
        ) : (
          <Card className="border-border">
            <CardContent className="pt-6 text-center">
              <Clock className="mx-auto mb-4 text-muted-foreground" size={48} />
              <p className="text-muted-foreground">No pending modification requests at this time.</p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
