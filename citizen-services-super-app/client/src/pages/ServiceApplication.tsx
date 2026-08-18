import { useState } from "react";
import { useAuth } from "@/_core/hooks/useAuth";
import { startLogin } from "@/const";
import { trpc } from "@/lib/trpc";
import { useLocation, useRoute } from "wouter";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, CheckCircle2, FileCheck2, Loader2, ShieldCheck } from "lucide-react";

export default function ServiceApplication() {
  const [, params] = useRoute("/apply/:serviceId");
  const [, navigate] = useLocation();
  const { user, loading } = useAuth();
  const serviceId = params?.serviceId ?? "";
  const { data: service, isLoading: serviceLoading } = trpc.services.getById.useQuery(serviceId, { enabled: Boolean(serviceId) });
  const submitApplication = trpc.applications.submit.useMutation();
  const [contact, setContact] = useState("");
  const [details, setDetails] = useState("");
  const [consent, setConsent] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  if (loading || serviceLoading) {
    return <div className="flex min-h-screen items-center justify-center bg-background"><Loader2 className="animate-spin text-primary" size={32} /></div>;
  }

  if (!user) {
    return <div className="flex min-h-screen items-center justify-center bg-background p-6"><Card className="w-full max-w-md border-border bg-card"><CardHeader><CardTitle>Sign in to begin</CardTitle><CardDescription>Applications are bound to the verified citizen account that owns the request.</CardDescription></CardHeader><CardContent><Button className="w-full" onClick={() => startLogin()}>Sign in securely</Button></CardContent></Card></div>;
  }

  if (user.role !== "citizen") {
    return <div className="flex min-h-screen items-center justify-center bg-background p-6"><Card className="w-full max-w-md border-border bg-card"><CardHeader><CardTitle>Citizen application route</CardTitle><CardDescription>Only a citizen account can initiate an application. Staff review happens in the separate case queue.</CardDescription></CardHeader><CardContent><Button variant="outline" className="w-full" onClick={() => navigate("/services")}>Return to catalog</Button></CardContent></Card></div>;
  }

  if (submitted) {
    return <div className="flex min-h-screen items-center justify-center bg-background p-6"><Card className="w-full max-w-lg border-primary/30 bg-card"><CardHeader><div className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-500/10 text-emerald-400"><CheckCircle2 /></div><CardTitle>Application submitted to the official queue</CardTitle><CardDescription>Your request has been recorded under your citizen account. No employee can directly edit the underlying record.</CardDescription></CardHeader><CardContent className="space-y-3"><Button className="w-full" onClick={() => navigate("/citizen-dashboard")}>Track application</Button><Button variant="outline" className="w-full" onClick={() => navigate("/services")}>Browse more services</Button></CardContent></Card></div>;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b border-border/80 bg-card/80 backdrop-blur-xl"><div className="container flex items-center justify-between py-5"><Button variant="ghost" onClick={() => navigate("/services")} className="gap-2 text-muted-foreground hover:text-foreground"><ArrowLeft size={17} /> Service directory</Button><div className="inline-flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-primary"><ShieldCheck size={16} /> Protected citizen workflow</div></div></header>
      <main className="container grid max-w-6xl gap-8 py-10 lg:grid-cols-[1fr_380px]">
        <div>
          <Badge variant="outline" className="border-primary/30 text-primary">{service?.moduleType?.replace(/_/g, " ") ?? "Government service"}</Badge>
          <h1 className="mt-4 text-4xl font-bold tracking-tight">{service?.name ?? "Government service"}</h1>
          <p className="mt-3 max-w-2xl text-lg leading-8 text-muted-foreground">{service?.description ?? "Start a secure application and we will route it to the responsible official department."}</p>
          <div className="mt-8 grid gap-3 sm:grid-cols-3"><div className="rounded-2xl border border-border bg-card p-4"><FileCheck2 className="mb-3 text-primary" size={20} /><p className="text-sm font-semibold">Owner initiated</p><p className="mt-1 text-xs text-muted-foreground">Your account starts the request.</p></div><div className="rounded-2xl border border-border bg-card p-4"><ShieldCheck className="mb-3 text-primary" size={20} /><p className="text-sm font-semibold">Verified route</p><p className="mt-1 text-xs text-muted-foreground">Status changes remain auditable.</p></div><div className="rounded-2xl border border-border bg-card p-4"><CheckCircle2 className="mb-3 text-primary" size={20} /><p className="text-sm font-semibold">Trackable</p><p className="mt-1 text-xs text-muted-foreground">Follow every state transition.</p></div></div>
        </div>

        <Card className="border-border bg-card shadow-2xl shadow-black/20"><CardHeader><CardTitle>Application details</CardTitle><CardDescription>Provide only the information needed to route this request. Supporting documents can be added through the secure vault.</CardDescription></CardHeader><CardContent className="space-y-5"><div className="space-y-2"><Label htmlFor="contact">Preferred contact</Label><Input id="contact" value={contact} onChange={event => setContact(event.target.value)} placeholder="Email or phone number" /></div><div className="space-y-2"><Label htmlFor="details">Request summary</Label><Textarea id="details" value={details} onChange={event => setDetails(event.target.value)} placeholder="Describe what you need help with..." className="min-h-32" /></div><label className="flex items-start gap-3 rounded-xl border border-border bg-background/50 p-3 text-sm text-muted-foreground"><input type="checkbox" checked={consent} onChange={event => setConsent(event.target.checked)} className="mt-1 accent-primary" /><span>I confirm that I am the owner of this request and consent to official processing under the platform’s verified workflow.</span></label><Button className="w-full" disabled={!consent || !details.trim() || submitApplication.isPending} onClick={async () => { await submitApplication.mutateAsync({ serviceId, applicationData: { contact, details, consent, submittedFrom: "citizen-service-catalog" } }); setSubmitted(true); }}><ShieldCheck className="mr-2" size={17} />{submitApplication.isPending ? "Submitting securely..." : "Submit application"}</Button>{submitApplication.error ? <p className="text-sm text-destructive">{submitApplication.error.message}</p> : null}</CardContent></Card>
      </main>
    </div>
  );
}
        
        
