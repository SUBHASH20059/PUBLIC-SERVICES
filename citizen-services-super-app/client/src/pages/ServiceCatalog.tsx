import { useMemo, useState } from "react";
import { useAuth } from "@/_core/hooks/useAuth";
import { startLogin } from "@/const";
import { trpc } from "@/lib/trpc";
import { useLocation } from "wouter";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Search, ShieldCheck, ArrowUpRight, Loader2, FileBadge, Map, BriefcaseBusiness, HeartPulse, GraduationCap, BusFront, Landmark, Scale, Zap, Bookmark, BookmarkCheck } from "lucide-react";

const categories = [
  { key: "all", label: "All services", icon: ShieldCheck },
  { key: "identity_cards", label: "Cards & identity", icon: FileBadge },
  { key: "civil_certificates", label: "Certificates", icon: Landmark },
  { key: "transport", label: "Transport", icon: BusFront },
  { key: "land_legal", label: "Land & legal", icon: Map },
  { key: "business_tax", label: "Business & tax", icon: BriefcaseBusiness },
  { key: "welfare", label: "Welfare", icon: HeartPulse },
  { key: "education_skills", label: "Education", icon: GraduationCap },
  { key: "health_agriculture", label: "Health & agriculture", icon: HeartPulse },
  { key: "utilities_employment", label: "Utilities & work", icon: Zap },
  { key: "grievance_justice", label: "Grievances & justice", icon: Scale },
];

const categoryLabels: Record<string, string> = Object.fromEntries(categories.map(category => [category.key, category.label]));
const levelLabels: Record<string, string> = {
  "lvl-regional": "Regional",
  "lvl-district": "District",
  "lvl-state": "State",
  "lvl-central": "Central",
  "lvl-national": "National",
};

export default function ServiceCatalog() {
  const { user } = useAuth();
  const [, navigate] = useLocation();
  const { data: services, isLoading } = trpc.services.list.useQuery();
  const { data: favorites } = trpc.favorites.list.useQuery(undefined, { enabled: Boolean(user) });
  const utils = trpc.useUtils();
  const toggleFavorite = trpc.favorites.toggle.useMutation({
    onSuccess: () => utils.favorites.list.invalidate(),
  });
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("all");
  const [favoritesOnly, setFavoritesOnly] = useState(false);
  const favoriteIds = useMemo(() => new Set((favorites ?? []).map(favorite => favorite.serviceId)), [favorites]);

  const filteredServices = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    return (services ?? []).filter(service => {
      const matchesCategory = category === "all" || service.moduleType === category;
      const matchesQuery = !normalized || [service.name, service.description, service.moduleType].some(value => value?.toLowerCase().includes(normalized));
      const matchesFavorites = !favoritesOnly || favoriteIds.has(service.id);
      return matchesCategory && matchesQuery && matchesFavorites;
    });
  }, [category, favoriteIds, favoritesOnly, query, services]);

  const handleFavorite = (serviceId: string) => {
    if (!user) {
      startLogin();
      return;
    }
    toggleFavorite.mutate({ serviceId });
  };

  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b border-border/80 bg-card/80 backdrop-blur-xl">
        <div className="container py-10">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/30 bg-primary/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-primary"><ShieldCheck size={14} /> Verified service directory</div>
              <h1 className="max-w-3xl text-4xl font-bold tracking-tight text-foreground md:text-5xl">Government services, organized around your life.</h1>
              <p className="mt-4 max-w-2xl text-base leading-7 text-muted-foreground">Browse cards, certificates, welfare, mobility, land, business, education, health, utility, and grievance services. Bookmark the services you use most for quick access later.</p>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row lg:flex-col xl:flex-row">
              <Button variant="outline" onClick={() => user ? navigate("/favorites") : startLogin()} className="gap-2 border-primary/30"><BookmarkCheck size={16} /> Saved services{user && favorites?.length ? ` (${favorites.length})` : ""}</Button>
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-3">
                <div className="rounded-2xl border border-border bg-background/70 p-4"><p className="text-2xl font-bold text-foreground">{services?.length ?? 0}</p><p className="text-xs text-muted-foreground">Catalog entries</p></div>
                <div className="rounded-2xl border border-border bg-background/70 p-4"><p className="text-2xl font-bold text-foreground">10</p><p className="text-xs text-muted-foreground">Service families</p></div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <main className="container py-8">
        <div className="grid gap-8 lg:grid-cols-[240px_1fr]">
          <aside className="space-y-2">
            <p className="px-3 pb-2 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">Browse by family</p>
            <button onClick={() => setFavoritesOnly(value => !value)} disabled={!user} className={`mb-3 flex w-full items-center gap-3 rounded-xl border px-3 py-2.5 text-left text-sm transition-colors ${favoritesOnly ? "border-primary/40 bg-primary text-primary-foreground" : "border-border bg-card text-muted-foreground hover:bg-accent hover:text-foreground"} disabled:cursor-not-allowed disabled:opacity-50`} title={user ? "Show only your saved services" : "Sign in to filter saved services"}><BookmarkCheck size={17} /><span>Saved services</span></button>
            {categories.map(item => {
              const Icon = item.icon;
              const active = category === item.key;
              return <button key={item.key} onClick={() => setCategory(item.key)} className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm transition-colors ${active ? "bg-primary text-primary-foreground shadow-lg shadow-primary/15" : "text-muted-foreground hover:bg-accent hover:text-foreground"}`}><Icon size={17} /><span>{item.label}</span></button>;
            })}
          </aside>

          <section>
            <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div><p className="text-sm text-muted-foreground">Showing <span className="font-semibold text-foreground">{filteredServices.length}</span> service entries</p></div>
              <div className="relative w-full sm:max-w-sm"><Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" size={17} /><Input value={query} onChange={event => setQuery(event.target.value)} placeholder="Search cards, certificates, tax..." className="h-11 border-border bg-card pl-10 text-foreground" /></div>
            </div>

            {isLoading ? <div className="flex min-h-64 items-center justify-center rounded-2xl border border-border bg-card"><Loader2 className="animate-spin text-primary" size={32} /></div> : filteredServices.length === 0 ? <Card className="border-border bg-card"><CardContent className="py-14 text-center"><p className="font-semibold text-foreground">{favoritesOnly ? "No saved services in this view" : "No matching services"}</p><p className="mt-1 text-sm text-muted-foreground">{favoritesOnly ? "Bookmark a service card to see it here." : "Try another search term or service family."}</p></CardContent></Card> : <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{filteredServices.map(service => { const isFavorite = favoriteIds.has(service.id); return <Card key={service.id} className="group flex h-full flex-col border-border bg-card/80 transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-xl hover:shadow-primary/5"><CardHeader className="pb-3"><div className="mb-3 flex items-center justify-between"><Badge variant="outline" className="border-primary/30 text-primary">{categoryLabels[service.moduleType] ?? service.moduleType}</Badge><Button variant="ghost" size="icon" aria-label={isFavorite ? `Remove ${service.name} from saved services` : `Save ${service.name}`} title={isFavorite ? "Remove from saved services" : "Save service"} onClick={() => handleFavorite(service.id)} disabled={toggleFavorite.isPending} className={isFavorite ? "text-primary hover:text-primary" : "text-muted-foreground hover:text-primary"}>{isFavorite ? <BookmarkCheck size={18} /> : <Bookmark size={18} />}</Button></div><CardTitle className="text-lg text-foreground">{service.name}</CardTitle><CardDescription className="leading-6 text-muted-foreground">{service.description}</CardDescription></CardHeader><CardContent className="mt-auto space-y-4"><div className="flex items-center justify-between text-xs"><span className="text-muted-foreground">Responsible level</span><span className="font-semibold text-foreground">{levelLabels[service.responsibleLevelId ?? ""] ?? "Department route"}</span></div><Button className="w-full" onClick={() => user?.role === "citizen" ? navigate(`/apply/${service.id}`) : startLogin()}>{user?.role === "citizen" ? "Start application" : "Sign in to apply"}</Button></CardContent></Card>; })}</div>}
          </section>
        </div>
      </main>
    </div>
  );
}
        
        
