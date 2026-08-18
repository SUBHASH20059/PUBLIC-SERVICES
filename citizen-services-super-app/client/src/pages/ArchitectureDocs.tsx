import { useState } from "react";
import { useAuth } from "@/_core/hooks/useAuth";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { ArrowDown, ArrowLeft, BookOpen, CheckCircle2, Database, Layout, Layers, MousePointerClick, Network, ShieldCheck, Terminal } from "lucide-react";
import { useLocation } from "wouter";

const architectureLayers = [
  {
    id: "presentation",
    label: "Presentation layer",
    eyebrow: "01 · Citizen experience",
    title: "Trustworthy screens for every role",
    description: "Responsive React views give citizens, employees, department administrators, and auditors clear paths through the same governed platform.",
    modules: ["Service directory", "Citizen dashboard", "Favorites & tracking", "Auditor console"],
    icon: Layout,
  },
  {
    id: "api",
    label: "API & routing layer",
    eyebrow: "02 · Guarded entry points",
    title: "Every request enters through a policy gate",
    description: "tRPC procedures validate inputs, establish the authenticated session, and apply role checks before business logic can run.",
    modules: ["Zod validation", "Protected procedures", "Session cookies", "RBAC middleware"],
    icon: Network,
  },
  {
    id: "workflow",
    label: "Workflow layer",
    eyebrow: "03 · Administrative process",
    title: "State machines protect citizen-owned records",
    description: "Applications, review queues, and modification requests move through explicit states so staff can propose decisions without directly editing records.",
    modules: ["Application state", "Case queue", "Digital verification", "5-tier scope"],
    icon: Layers,
  },
  {
    id: "persistence",
    label: "Persistence layer",
    eyebrow: "04 · System of record",
    title: "Structured data with controlled ownership",
    description: "MySQL and Drizzle ORM keep service, user, application, favorite, and administrative records consistent and queryable.",
    modules: ["Relational schema", "Unique constraints", "Owner scoping", "Migration discipline"],
    icon: Database,
  },
  {
    id: "audit",
    label: "Audit & storage layer",
    eyebrow: "05 · Evidence and records",
    title: "Actions remain visible and documents remain protected",
    description: "Append-only audit events and S3-backed document storage make important actions traceable while keeping file bytes outside the relational database.",
    modules: ["Tamper-evident events", "Digital signatures", "S3 document vault", "Presigned access"],
    icon: ShieldCheck,
  },
] as const;

export default function ArchitectureDocs() {
  const { user } = useAuth();
  const [, navigate] = useLocation();
  const [selectedLayerId, setSelectedLayerId] = useState<(typeof architectureLayers)[number]["id"]>("presentation");
  const selectedLayer = architectureLayers.find(layer => layer.id === selectedLayerId) ?? architectureLayers[0];
  const SelectedIcon = selectedLayer.icon;

  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b border-border/80 bg-card/80 backdrop-blur-xl">
        <div className="container py-10">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/30 bg-primary/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-primary"><BookOpen size={14} /> System Design Specification</div>
              <h1 className="max-w-3xl text-4xl font-bold tracking-tight text-foreground md:text-5xl">Architectural Blueprint & Engineering Standards</h1>
              <p className="mt-4 max-w-2xl text-base leading-7 text-muted-foreground">Explore the platform architecture as an interactive system map, then expand only the design sections you need to review.</p>
            </div>
            <div className="flex gap-3">
              <Button variant="outline" onClick={() => navigate("/services")} className="gap-2"><ArrowLeft size={16} /> Service directory</Button>
              <Button onClick={() => navigate(user ? "/citizen-dashboard" : "/services")} className="gap-2">{user ? "Dashboard" : "Get started"}</Button>
            </div>
          </div>
        </div>
      </header>

      <main className="container space-y-8 py-10">
        <Card className="overflow-hidden border-primary/20 bg-card/80 shadow-xl shadow-primary/5">
          <CardHeader className="border-b border-border/70 bg-primary/[0.04]">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <div className="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-primary"><MousePointerClick size={14} /> Interactive architecture map</div>
                <CardTitle className="text-2xl">Select a layer to inspect its responsibility</CardTitle>
                <CardDescription className="mt-2 max-w-2xl">The arrows represent the normal request path. The policy boundary is enforced before any record or document can be changed.</CardDescription>
              </div>
              <Badge variant="outline" className="w-fit border-primary/30 text-primary">5 governed layers</Badge>
            </div>
          </CardHeader>
          <CardContent className="grid gap-8 p-6 lg:grid-cols-[1fr_0.8fr] lg:p-8">
            <div className="flex flex-col items-stretch gap-2" aria-label="Interactive architecture layers">
              {architectureLayers.map((layer, index) => {
                const Icon = layer.icon;
                const isSelected = selectedLayerId === layer.id;
                return <div key={layer.id} className="flex flex-col items-stretch gap-2">
                  <button type="button" aria-pressed={isSelected} onClick={() => setSelectedLayerId(layer.id)} className={`group flex items-center gap-4 rounded-2xl border p-4 text-left transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary ${isSelected ? "border-primary/60 bg-primary/10 shadow-lg shadow-primary/10" : "border-border bg-background/50 hover:border-primary/30 hover:bg-primary/[0.04]"}`}>
                    <span className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${isSelected ? "bg-primary text-primary-foreground" : "bg-accent text-primary"}`}><Icon size={20} /></span>
                    <span className="min-w-0 flex-1"><span className="block text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">{layer.eyebrow}</span><span className="mt-1 block font-semibold text-foreground">{layer.label}</span></span>
                    <span className={`h-2.5 w-2.5 rounded-full ${isSelected ? "bg-primary shadow-[0_0_0_5px_hsl(var(--primary)/0.12)]" : "bg-muted-foreground/40"}`} />
                  </button>
                  {index < architectureLayers.length - 1 ? <div className="flex h-5 items-center justify-center text-primary/50"><ArrowDown size={16} /></div> : null}
                </div>;
              })}
            </div>
            <div className="rounded-2xl border border-primary/25 bg-background/70 p-6" aria-live="polite">
              <div className="mb-5 flex items-center justify-between"><div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary"><SelectedIcon size={24} /></div><Badge className="bg-primary/15 text-primary hover:bg-primary/15">Selected layer</Badge></div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-primary">{selectedLayer.eyebrow}</p>
              <h2 className="mt-2 text-2xl font-bold tracking-tight text-foreground">{selectedLayer.title}</h2>
              <p className="mt-3 text-sm leading-7 text-muted-foreground">{selectedLayer.description}</p>
              <div className="mt-6 space-y-3"><p className="text-xs font-semibold uppercase tracking-[0.15em] text-muted-foreground">Key components</p>{selectedLayer.modules.map(module => <div key={module} className="flex items-center gap-2 text-sm text-foreground"><CheckCircle2 size={16} className="text-primary" /> {module}</div>)}</div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-border bg-card/80 shadow-xl">
          <CardHeader><div className="flex items-center gap-3"><div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10 text-primary"><Terminal size={20} /></div><div><CardTitle className="text-2xl">Detailed design sections</CardTitle><CardDescription>Expand a section to review the formal design notes without losing your place.</CardDescription></div></div></CardHeader>
          <CardContent className="pt-0">
            <Accordion type="multiple" defaultValue={["architecture"]} className="w-full">
              <AccordionItem value="architecture" className="border-border">
                <AccordionTrigger className="py-5 hover:no-underline"><span className="flex items-center gap-3"><Layers size={18} className="text-primary" /><span><span className="block text-base font-semibold text-foreground">1. Architectural Design</span><span className="mt-1 block text-xs font-normal text-muted-foreground">The overarching structure of the software system</span></span></span></AccordionTrigger>
                <AccordionContent className="text-muted-foreground"><p className="leading-7">The platform decomposes the administrative burden into specialized modules while maintaining strict legal and ownership boundaries. The interactive map above shows the five-layer request path from presentation to evidence and storage.</p><div className="mt-4 grid gap-3 sm:grid-cols-2"><div className="rounded-xl border border-border bg-background/50 p-4"><p className="font-semibold text-foreground">Policy-first routing</p><p className="mt-1 text-xs leading-5">Authentication, input validation, and RBAC gates execute before business logic.</p></div><div className="rounded-xl border border-border bg-background/50 p-4"><p className="font-semibold text-foreground">Evidence by design</p><p className="mt-1 text-xs leading-5">Sensitive operations produce an audit event that can be reviewed by system auditors.</p></div></div></AccordionContent>
              </AccordionItem>

              <AccordionItem value="data" className="border-border">
                <AccordionTrigger className="py-5 hover:no-underline"><span className="flex items-center gap-3"><Database size={18} className="text-primary" /><span><span className="block text-base font-semibold text-foreground">2. Data Design</span><span className="mt-1 block text-xs font-normal text-muted-foreground">Databases, files, and data structures</span></span></span></AccordionTrigger>
                <AccordionContent className="text-muted-foreground"><p className="leading-7">Data design specifies how information is persisted, indexed, and manipulated securely. Core entities include users and authentication, the 69-service catalog, citizen applications, modification requests, per-user favorites, and append-only audit logs.</p><div className="mt-4 flex flex-wrap gap-2"><Badge variant="outline">Owner-scoped records</Badge><Badge variant="outline">Unique constraints</Badge><Badge variant="outline">Drizzle ORM</Badge><Badge variant="outline">S3 file references</Badge></div></AccordionContent>
              </AccordionItem>

              <AccordionItem value="interface" className="border-border">
                <AccordionTrigger className="py-5 hover:no-underline"><span className="flex items-center gap-3"><Layout size={18} className="text-primary" /><span><span className="block text-base font-semibold text-foreground">3. Interface Design</span><span className="mt-1 block text-xs font-normal text-muted-foreground">Screens, menus, forms, and reports</span></span></span></AccordionTrigger>
                <AccordionContent className="text-muted-foreground"><p className="leading-7">The citizen interface prioritizes clear service discovery, saved services, application tracking, and secure document access. Administrative interfaces expose only the queue, review, and audit controls permitted by the actor's role and administrative level.</p><div className="mt-4 grid gap-3 sm:grid-cols-2"><div className="rounded-xl border border-border bg-background/50 p-4"><p className="font-semibold text-foreground">Citizen paths</p><p className="mt-1 text-xs">Directory → favorite → application → status receipt.</p></div><div className="rounded-xl border border-border bg-background/50 p-4"><p className="font-semibold text-foreground">Governance paths</p><p className="mt-1 text-xs">Assigned case → proposal → verification → audited decision.</p></div></div></AccordionContent>
              </AccordionItem>

              <AccordionItem value="components" className="border-border">
                <AccordionTrigger className="py-5 hover:no-underline"><span className="flex items-center gap-3"><Terminal size={18} className="text-primary" /><span><span className="block text-base font-semibold text-foreground">4. Component-Level Design</span><span className="mt-1 block text-xs font-normal text-muted-foreground">Internal module logic and infrastructure configuration</span></span></span></AccordionTrigger>
                <AccordionContent className="text-muted-foreground"><p className="leading-7">The server composes modular tRPC routers for services, favorites, applications, modifications, and audits. The runtime is a containerized Node.js application behind TLS, with managed MySQL persistence, startup migrations, and seeded service catalog data.</p><div className="mt-4 rounded-xl border border-border bg-background/50 p-4 font-mono text-xs leading-6 text-muted-foreground"><span className="text-primary">server/routers.ts</span> → policy gates → domain procedure → database write → audit event</div></AccordionContent>
              </AccordionItem>
            </Accordion>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
        
        
