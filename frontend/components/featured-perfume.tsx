import Link from "next/link";
import { PerfumeSummary } from "@/lib/types";

export function FeaturedPerfume({ perfume }: { perfume: PerfumeSummary }) {
  const accords = perfume.mainAccords.slice(0, 4);

  return (
    <Link
      href={`/perfumes/${perfume.id}`}
      className="group relative block w-full max-w-sm rounded-4xl bg-card p-8 text-card-foreground shadow-md ring-1 ring-foreground/5 dark:ring-foreground/10"
    >
      <div className="flex flex-col gap-5">
        {accords.map((accord, i) => (
          <div key={accord} className="flex items-center gap-4">
            <span
              className="h-px bg-primary origin-left transition-[width] duration-300 group-hover:w-full"
              style={{ width: `${100 - i * 22}%`, opacity: 1 - i * 0.16 }}
            />
            <span className="shrink-0 text-xs tracking-wide text-muted-foreground">
              {accord}
            </span>
          </div>
        ))}
      </div>

      <div className="mt-8 border-t border-border pt-6">
        <p className="text-xs text-muted-foreground">{perfume.brandName}</p>
        <h3 className="font-serif text-xl mt-1">{perfume.name}</h3>
        {perfume.year && (
          <p className="mt-1 text-xs text-muted-foreground">{perfume.year}</p>
        )}
      </div>
    </Link>
  );
}
