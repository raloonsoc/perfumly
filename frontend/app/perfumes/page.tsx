import { PerfumeCard } from "@/components/perfume-card";
import { CatalogueFilters } from "@/components/catalogue-filters";
import { PageTransition } from "@/components/page-transition";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { apiFetch } from "@/lib/api";
import { PerfumeSummary, PageResponse } from "@/lib/types";

const PAGE_SIZE = 24;

function buildHref(params: Record<string, string | undefined>) {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value) query.set(key, value);
  }
  const qs = query.toString();
  return qs ? `/perfumes?${qs}` : "/perfumes";
}

export default async function CataloguePage({
  searchParams,
}: PageProps<"/perfumes">) {
  const params = await searchParams;
  const gender = typeof params.gender === "string" ? params.gender : undefined;
  const search = typeof params.search === "string" ? params.search : undefined;
  const brandId = typeof params.brandId === "string" ? params.brandId : undefined;
  const brandName = typeof params.brandName === "string" ? params.brandName : undefined;
  const page = typeof params.page === "string" ? Number(params.page) || 0 : 0;

  const query = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) });
  if (gender) query.set("gender", gender);
  if (search) query.set("search", search);
  if (brandId) query.set("brandId", brandId);

  const data = await apiFetch<PageResponse<PerfumeSummary>>(`/api/perfumes?${query.toString()}`);

  return (
    <PageTransition>
    <main className="pt-24">
      <div className="container mx-auto px-4 py-12">
        <h1 className="font-serif text-3xl md:text-4xl">Catalogue</h1>
        <p className="mt-2 text-muted-foreground">
          {data.totalElements.toLocaleString("en-US")} fragrances
        </p>

        <div className="mt-8">
          <CatalogueFilters gender={gender} search={search} brandId={brandId} brandName={brandName}>
            {data.content.length === 0 ? (
              <p className="mt-16 text-center text-muted-foreground">
                No fragrances match your search.
              </p>
            ) : (
              <div className="mt-8 grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
                {data.content.map((perfume) => (
                  <PerfumeCard key={perfume.id} perfume={perfume} />
                ))}
              </div>
            )}

            {data.totalPages > 1 && (
              <Pagination className="mt-12">
                <PaginationContent>
                  <PaginationItem>
                    <PaginationPrevious
                      href={buildHref({ gender, search, brandId, brandName, page: page > 0 ? String(page - 1) : undefined })}
                      aria-disabled={page === 0}
                      className={page === 0 ? "pointer-events-none opacity-50" : undefined}
                    />
                  </PaginationItem>
                  {Array.from({ length: data.totalPages }, (_, i) => i)
                    .filter((i) => i === 0 || i === data.totalPages - 1 || Math.abs(i - page) <= 1)
                    .map((i, idx, arr) => (
                      <PaginationItem key={i}>
                        {idx > 0 && arr[idx - 1] !== i - 1 ? (
                          <span className="px-2 text-muted-foreground">…</span>
                        ) : null}
                        <PaginationLink href={buildHref({ gender, search, brandId, brandName, page: String(i) })} isActive={i === page}>
                          {i + 1}
                        </PaginationLink>
                      </PaginationItem>
                    ))}
                  <PaginationItem>
                    <PaginationNext
                      href={buildHref({ gender, search, brandId, brandName, page: page < data.totalPages - 1 ? String(page + 1) : undefined })}
                      aria-disabled={page === data.totalPages - 1}
                      className={page === data.totalPages - 1 ? "pointer-events-none opacity-50" : undefined}
                    />
                  </PaginationItem>
                </PaginationContent>
              </Pagination>
            )}
          </CatalogueFilters>
        </div>
      </div>
      </main>
    </PageTransition>
  );
}
