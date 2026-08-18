import { useAuth } from "@/_core/hooks/useAuth";
import { startLogin } from "@/const";
import { trpc } from "@/lib/trpc";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ArrowLeft, ArrowUpRight, BookmarkCheck, Loader2, Search, ShieldCheck } from "lucide-react";
import { useLocation } from "wouter";

const levelLabels: Record<string, string> = {
  "lvl-regional": "Regional",
  "lvl-district": "District",
  "lvl-state": "State",
  "lvl-central": "Central",
  "lvl-national": "National",
};

export default function Favorites() {
  const { user, loading } = useAuth();
  const [, navigate] = useLocation();
  const utils = trpc.useUtils();
  const { data: favorites, isLoading } = trpc.favorites.list.useQuery(undefined, { enabled: Boolean(user) });
  const toggleFavorite = trpc.favorites.toggle.useMutation({
    onSuccess: () => utils.favorites.list.invalidate(),
  });

  if (loading || (user && isLoading)) {
    return <div className="flex min-h-screen items-center justify-center bg-background"><Loader2 className="animate-spin text-primary" size={32} /></div>;
  }

  if (!user) {
    return <div className="flex min-h-screen items-center justify-center bg-background p-6"><Card className="w-full max-w-md border-border bg-card"><CardHeader><div className="mb-2 flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary"><BookmarkCheck size={22} /></div><CardTitle>Sign in to view saved services</CardTitle><CardDescription>Your favorites are private and stored against your authenticated account.</CardDescription></CardHeader><CardContent className="space-y-3"><Button className="w-full" onClick={() => startLogin()}>Sign in securely</Button><Button variant="outline" className="w-full" onClick={() => navigate("/services")}>Browse services</Button></CardContent></Card></div>;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b border-border/80 bg-card/80 backdrop-blur-xl"><div className="container flex flex-col gap-5 py-8 sm:flex-row sm:items-end sm:justify-between"><div><div className="mb-3 inline-flex items-center gap-2 rounded-full border border-primary/30 bg-primary/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-primary"><ShieldCheck size={14} /> Private quick access</div><h1 className="text-4xl font-bold tracking-tight">Saved government services</h1><p className="mt-2 text-muted-foreground">Your bookmarked service cards are ready whenever you need them.</p></div><Button variant="outline" onClick={() => navigate("/services")} className="gap-2"><ArrowLeft size={16} /> Browse directory</Button></div></header>
      <main className="container py-8">
        {favorites?.length ? <div className="mb-6 flex items-center justify-between rounded-2xl border border-primary/20 bg-primary/5 p-4"><div><p className="font-semibold">{favorites.length} saved {favorites.length === 1 ? "service" : "services"}</p><p className="text-sm text-muted-foreground">Only you can view or change this list.</p></div><Search size={19} className="text-primary" /></div> : null}
        {!favorites?.length ? <Card className="border-dashed border-border bg-card"><CardContent className="flex flex-col items-center justify-center py-20 text-center"><BookmarkCheck size={36} className="mb-4 text-primary" /><p className="text-lg font-semibold">No saved services yet</p><p className="mt-2 max-w-md text-sm leading-6 text-muted-foreground">Use the bookmark icon on any service card to keep it here for faster access later.</p><Button className="mt-6" onClick={() => navigate("/services")}>Explore services</Button></CardContent></Card> : <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{favorites.map(service => <Card key={service.id} className="group flex h-full flex-col border-border bg-card/80 transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-xl hover:shadow-primary/5"><CardHeader className="pb-3"><div className="mb-3 flex items-center justify-between"><Badge variant="outline" className="border-primary/30 text-primary">{service.moduleType.replace(/_/g, " ")}</Badge><BookmarkCheck size={18} className="text-primary" /></div><CardTitle className="text-lg">{service.name}</CardTitle><CardDescription className="leading-6">{service.description}</CardDescription></CardHeader><CardContent className="mt-auto space-y-4"><div className="flex items-center justify-between text-xs"><span className="text-muted-foreground">Responsible level</span><span className="font-semibold">{levelLabels[service.responsibleLevelId ?? ""] ?? "Department route"}</span></div><div className="grid gap-2 sm:grid-cols-2"><Button onClick={() => navigate(`/apply/${service.serviceId}`)} className="gap-2">Open service <ArrowUpRight size={15} /></Button><Button variant="outline" onClick={() => toggleFavorite.mutate({ serviceId: service.serviceId })} disabled={toggleFavorite.isPending}>Remove</Button></div></CardContent></Card>)}</div>}
      </main>
    </div>
  );
}
        
        
