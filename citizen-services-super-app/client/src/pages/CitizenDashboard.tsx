import { useAuth } from "@/_core/hooks/useAuth";
import { trpc } from "@/lib/trpc";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loader2, FileText, CheckCircle, Clock, AlertCircle } from "lucide-react";
import { useLocation } from "wouter";

const SERVICE_TRACKS = {
  civil: {
    name: "Civil & Personal Services",
    color: "bg-blue-900",
    services: ["Marriage Registration", "PAN Card Processing", "Driving License"],
  },
  land: {
    name: "Land & Legal Services",
    color: "bg-indigo-900",
    services: ["Land Records", "Land Registration", "Deed Tracking"],
  },
  business: {
    name: "Business & Startup Services",
    color: "bg-purple-900",
    services: ["Startup Registration", "Compliance Tracking", "Innovation Protection"],
  },
};

const STATUS_CONFIG = {
  SUBMITTED: { icon: Clock, color: "bg-yellow-600", label: "Pending" },
  APPROVED: { icon: CheckCircle, color: "bg-green-600", label: "Approved" },
  REJECTED: { icon: AlertCircle, color: "bg-red-600", label: "Rejected" },
  IN_PROGRESS: { icon: Loader2, color: "bg-blue-600", label: "In Progress" },
};

export default function CitizenDashboard() {
  const { user, loading } = useAuth();
  const [, navigate] = useLocation();
  const { data: services, isLoading: servicesLoading } = trpc.services.list.useQuery();
  const { data: applications, isLoading: appsLoading } = trpc.applications.listMyCitizen.useQuery(
    undefined,
    { enabled: !!user && user.role === "citizen" }
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Loader2 className="animate-spin text-primary" size={48} />
      </div>
    );
  }

  if (!user || user.role !== "citizen") {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Access Denied</CardTitle>
            <CardDescription>Only citizens can access this dashboard.</CardDescription>
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
          <h1 className="text-4xl font-bold text-foreground mb-2">Welcome, {user.fullName}</h1>
          <p className="text-muted-foreground">Manage your government services and applications in one secure place.</p>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-12">
        {/* Application Status Overview */}
        {applications && applications.length > 0 && (
          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-6 text-foreground">Your Applications</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {applications.map((app) => {
                const statusConfig = STATUS_CONFIG[app.status as keyof typeof STATUS_CONFIG] || STATUS_CONFIG.SUBMITTED;
                const StatusIcon = statusConfig.icon;
                return (
                  <Card key={app.id} className="border-border hover:shadow-lg transition-shadow">
                    <CardHeader>
                      <div className="flex items-start justify-between">
                        <div>
                          <CardTitle className="text-lg text-foreground">Application #{app.id.slice(0, 8)}</CardTitle>
                          <CardDescription className="text-sm text-muted-foreground">
                            Submitted: {new Date(app.submissionDate).toLocaleDateString()}
                          </CardDescription>
                        </div>
                        <Badge className={`${statusConfig.color} text-white`}>
                          {statusConfig.label}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <p className="text-sm text-muted-foreground mb-4">
                        Last updated: {new Date(app.lastUpdatedAt).toLocaleDateString()}
                      </p>
                      <Button
                        variant="outline"
                        size="sm"
                        className="w-full border-border text-foreground hover:bg-accent"
                        onClick={() => navigate(`/application/${app.id}`)}
                      >
                        View Details
                      </Button>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </section>
        )}

        {/* Service Tracks */}
        <section>
          <h2 className="text-2xl font-bold mb-6 text-foreground">Available Services</h2>
          <div className="space-y-8">
            {Object.entries(SERVICE_TRACKS).map(([key, track]) => (
              <div key={key}>
                <h3 className="text-xl font-semibold mb-4 text-foreground">{track.name}</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {track.services.map((serviceName) => (
                    <Card key={serviceName} className={`${track.color} border-0 text-white hover:shadow-lg transition-shadow`}>
                      <CardHeader>
                        <CardTitle className="text-lg flex items-center gap-2">
                          <FileText size={20} />
                          {serviceName}
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        <p className="text-sm text-gray-200 mb-4">
                          Apply for {serviceName.toLowerCase()} with our secure digital process.
                        </p>
                        <Button
                          className="w-full bg-white text-gray-900 hover:bg-gray-100"
                          onClick={() => navigate(`/apply/${serviceName.toLowerCase().replace(/\s+/g, "-")}`)}
                        >
                          Apply Now
                        </Button>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Quick Actions */}
        <section className="mt-12 pt-8 border-t border-border">
          <h2 className="text-2xl font-bold mb-6 text-foreground">Quick Actions</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Button
              variant="outline"
              className="h-16 border-border text-foreground hover:bg-accent"
              onClick={() => navigate("/document-vault")}
            >
              <FileText className="mr-2" />
              View Document Vault
            </Button>
            <Button
              variant="outline"
              className="h-16 border-border text-foreground hover:bg-accent"
              onClick={() => navigate("/ai-assistant")}
            >
              <Loader2 className="mr-2" />
              Get AI Assistance
            </Button>
          </div>
        </section>
      </div>
    </div>
  );
}
