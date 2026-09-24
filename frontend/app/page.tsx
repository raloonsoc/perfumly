import Link from "next/link";
import { Heart, MessageSquare, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FeaturedPerfume } from "@/components/featured-perfume";
import { PerfumeCard } from "@/components/perfume-card";
import { apiFetch } from "@/lib/api";
import { PerfumeSummary, PageResponse } from "@/lib/types";

const memberBenefits = [
  {
    icon: MessageSquare,
    title: "Share your opinion",
    description: "Rate and review fragrances to help others find their next scent.",
  },
  {
    icon: Heart,
    title: "Keep a collection",
    description: "Save fragrances you love or want to try to your own favourites list.",
  },
  {
    icon: Sparkles,
    title: "Get matched",
    description: "Unlock recommendations tailored to the notes and accords you gravitate towards.",
  },
];

export default async function Home() {
  const data = await apiFetch<PageResponse<PerfumeSummary>>("/api/perfumes");
  const featured = data.content[0];
  const highlights = data.content.slice(1, 7);

  return (
    <main className="pt-24">
      <section className="relative">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 -z-10 overflow-hidden"
        >
          <div
            className="absolute inset-0"
            style={{
              background:
                "radial-gradient(ellipse 50% 40% at 75% 45%, color-mix(in oklch, var(--primary) 10%, transparent), transparent 70%)",
            }}
          />
        </div>
        <div className="container mx-auto px-4 py-16">
          <div className="grid md:grid-cols-2 gap-10 items-center">
            <div className="text-center md:text-left">
              <h1 className="font-serif text-4xl md:text-5xl leading-tight text-balance">
                A record of scent, written by everyone who&apos;s worn it
              </h1>
              <p className="mt-4 text-muted-foreground text-lg max-w-md mx-auto md:mx-0 text-balance">
                Discover, compare and share your favourite fragrances.
              </p>
              <div className="mt-8 flex justify-center md:justify-start">
                <Button size="lg" nativeButton={false} render={<Link href="/perfumes" />}>
                  Browse catalogue
                </Button>
              </div>
              <p className="mt-10 text-sm text-muted-foreground">
                {data.totalElements.toLocaleString("en-US")} fragrances catalogued
              </p>
            </div>

            {featured && (
              <div className="flex justify-center md:justify-end">
                <FeaturedPerfume perfume={featured} />
              </div>
            )}
          </div>
        </div>
      </section>

      {highlights.length > 0 && (
        <section className="container mx-auto px-4 py-16">
          <div className="flex items-baseline justify-between gap-4 mb-8">
            <h2 className="font-serif text-2xl md:text-3xl">Recently added</h2>
            <Link
              href="/perfumes"
              className="text-sm text-muted-foreground hover:text-foreground transition-colors shrink-0"
            >
              View all
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
            {highlights.map((perfume) => (
              <PerfumeCard key={perfume.id} perfume={perfume} />
            ))}
          </div>
        </section>
      )}

      <section className="container mx-auto px-4 py-16">
        <div className="rounded-4xl bg-card ring-1 ring-foreground/5 dark:ring-foreground/10 px-6 py-12 md:px-16 md:py-16">
          <div className="max-w-xl">
            <h2 className="font-serif text-2xl md:text-3xl text-balance">
              Make Perfumly yours
            </h2>
            <p className="mt-3 text-muted-foreground text-balance">
              Create a free account to join the conversation around scent.
            </p>
          </div>

          <div className="mt-10 grid sm:grid-cols-3 gap-8">
            {memberBenefits.map(({ icon: Icon, title, description }) => (
              <div key={title}>
                <Icon className="size-5 text-primary" strokeWidth={1.75} />
                <h3 className="font-serif text-lg mt-3">{title}</h3>
                <p className="mt-1.5 text-sm text-muted-foreground text-balance">
                  {description}
                </p>
              </div>
            ))}
          </div>

          <div className="mt-10 flex flex-wrap gap-3">
            <Button size="lg" nativeButton={false} render={<Link href="/register" />}>
              Create your account
            </Button>
            <Button size="lg" variant="outline" nativeButton={false} render={<Link href="/login" />}>
              Log in
            </Button>
          </div>
        </div>
      </section>
    </main>
  );
}
