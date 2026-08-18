import { useAuth } from "@/_core/hooks/useAuth";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useLocation } from "wouter";
import { Loader2, Shield, Users, FileText, BarChart3 } from "lucide-react";
import { startLogin } from "@/const";

export default function Home() {
  const { user, loading, logout } = useAuth();
  const [, navigate] = useLocation();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Loader2 className="animate-spin text-primary" size={48} />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Navigation Header */}
      <div className="border-b border-border bg-card sticky top-0 z-50">
        <div className="container flex items-center justify-between py-4">
          <div className="flex items-center gap-3">
            <Shield size={32} className="text-primary" />
            <h1 className="text-2xl font-bold text-foreground">Citizen Services Super App</h1>
          </div>
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              className="text-muted-foreground hover:text-foreground"
              onClick={() => navigate("/architecture")}
            >
              Architecture Spec
            </Button>
            {user ? (
              <>
                <span className="text-sm text-muted-foreground">
                  {user.fullName} ({user.role})
                </span>
              <Button
                variant="outline"
                className="border-border text-foreground hover:bg-accent"
                onClick={async () => await logout()}
              >
                Logout
              </Button>
              </>
            ) : (
              <Button
                className="bg-primary text-primary-foreground hover:bg-primary/90"
                onClick={() => startLogin()}
              >
                Sign In
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Hero Section */}
      <div className="border-b border-border bg-gradient-to-b from-card to-background">
        <div className="container py-20 text-center">
          <h2 className="text-5xl font-bold text-foreground mb-6">
            Unified Government Services Platform
          </h2>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto mb-8">
            Secure, transparent, and citizen-centric digital governance. Access all government services
            with strict security controls, immutable audit trails, and trusted workflows.
          </p>
          <div className="flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Button
              size="lg"
              className="bg-primary text-primary-foreground hover:bg-primary/90"
              onClick={() => navigate("/services")}
            >
              Browse government services
            </Button>
            {!user && (
              <Button
                size="lg"
                variant="outline"
                className="border-border text-foreground hover:bg-accent"
                onClick={() => startLogin()}
              >
                Sign in to apply
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-16">
        {user ? (
          <>
            {/* Citizen Dashboard */}
            {user.role === "citizen" && (
              <section className="mb-12">
                <h3 className="text-3xl font-bold text-foreground mb-6">Your Dashboard</h3>
                <Card className="border-border hover:shadow-lg transition-shadow cursor-pointer" onClick={() => navigate("/citizen-dashboard")}>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <FileText size={24} className="text-primary" />
                      Citizen Dashboard
                    </CardTitle>
                    <CardDescription>
                      Track your applications, access services, and manage your documents securely.
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
                      Open Dashboard
                    </Button>
                  </CardContent>
                </Card>
              </section>
            )}

            {/* Employee Case Queue */}
            {(user.role === "employee" || user.role === "department_admin") && (
              <section className="mb-12">
                <h3 className="text-3xl font-bold text-foreground mb-6">Case Management</h3>
                <Card className="border-border hover:shadow-lg transition-shadow cursor-pointer" onClick={() => navigate("/case-queue")}>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <Users size={24} className="text-primary" />
                      Case Queue
                    </CardTitle>
                    <CardDescription>
                      Review and process pending modification requests with full audit logging.
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
                      View Cases
                    </Button>
                  </CardContent>
                </Card>
              </section>
            )}

            {/* Audit Log Viewer */}
            {user.role === "system_auditor" && (
              <section className="mb-12">
                <h3 className="text-3xl font-bold text-foreground mb-6">System Auditing</h3>
                <Card className="border-border hover:shadow-lg transition-shadow cursor-pointer" onClick={() => navigate("/audit-logs")}>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <BarChart3 size={24} className="text-primary" />
                      Immutable Audit Logs
                    </CardTitle>
                    <CardDescription>
                      Review tamper-proof records of all system actions and user activities.
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Button className="bg-primary text-primary-foreground hover:bg-primary/90">
                      View Logs
                    </Button>
                  </CardContent>
                </Card>
              </section>
            )}
          </>
        ) : (
          <>
            {/* Feature Overview */}
            <section className="mb-12">
              <h3 className="text-3xl font-bold text-foreground mb-6">Platform Features</h3>
              <div className="mb-6 flex items-center justify-between gap-4 rounded-2xl border border-primary/20 bg-primary/5 p-5">
                <div><p className="font-semibold text-foreground">Looking for a specific card or service?</p><p className="mt-1 text-sm text-muted-foreground">Search the expanded directory by service family, authority level, or name.</p></div>
                <Button onClick={() => navigate("/services")} className="shrink-0">Open directory</Button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <Card className="border-border">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <Shield size={20} className="text-primary" />
                      Secure Access
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">
                      Role-based access control with strict permission enforcement across all services.
                    </p>
                  </CardContent>
                </Card>

                <Card className="border-border">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <FileText size={20} className="text-primary" />
                      Civil Services
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">
                      Marriage registration, PAN processing, and driving license issuance.
                    </p>
                  </CardContent>
                </Card>

                <Card className="border-border">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <Users size={20} className="text-primary" />
                      Land & Legal
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">
                      Land records, property registration, and legal document management.
                    </p>
                  </CardContent>
                </Card>

                <Card className="border-border">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <BarChart3 size={20} className="text-primary" />
                      Business Services
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">
                      Startup registration, compliance tracking, and innovation protection.
                    </p>
                  </CardContent>
                </Card>

                <Card className="border-border">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <Shield size={20} className="text-primary" />
                      Immutable Audit Trail
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">
                      Tamper-proof logging of all actions for complete transparency and accountability.
                    </p>
                  </CardContent>
                </Card>

                <Card className="border-border">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-foreground">
                      <FileText size={20} className="text-primary" />
                      Digital Signatures
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground">
                      Secure cryptographic signatures for all official documents and modifications.
                    </p>
                  </CardContent>
                </Card>
              </div>
            </section>
          </>
        )}
      </div>

      {/* Footer */}
      <div className="border-t border-border bg-card mt-16">
        <div className="container py-8 text-center text-muted-foreground">
          <p>
            Citizen Services Super App &bull; Unified Digital Governance Platform
          </p>
          <p className="text-sm mt-2">
            Built with security, transparency, and citizen trust at the core.
          </p>
        </div>
      </div>
    </div>
  );
}
