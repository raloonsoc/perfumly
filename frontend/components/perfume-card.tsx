import { PerfumeSummary } from "@/lib/types";
import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils";
import { accordGradient } from "@/lib/accord-color";

export function PerfumeCard({ perfume, className }: { perfume: PerfumeSummary; className?: string }) {
  const accords = perfume.mainAccords.slice(0, 2);
  const initial = perfume.brandName.trim().charAt(0).toUpperCase();

  return (
    <Link href={`/perfumes/${perfume.id}`} className={cn("block", className)}>
      <Card size="sm" className="group overflow-hidden hover:shadow-md transition-shadow p-0 gap-0">
        <div
          className="aspect-[4/3] flex items-center justify-center"
          style={{ background: accordGradient(perfume.mainAccords[0] ?? perfume.brandName) }}
        >
          <span className="font-serif text-5xl text-white/90">{initial}</span>
        </div>
        <CardContent className="p-4 space-y-3">
          <div>
            <p className="text-xs text-muted-foreground uppercase">{perfume.brandName}</p>
            <h3 className="font-serif text-base leading-snug line-clamp-2 group-hover:text-primary transition-colors">
              {perfume.name}
            </h3>
          </div>
          <div className="flex flex-wrap gap-2">
            {accords.map((accord) => (
              <Badge key={accord} variant="accent" className="text-xs">
                {accord}
              </Badge>
            ))}
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}
