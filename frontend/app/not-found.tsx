import Link from "next/link"
import { Compass } from "lucide-react"
import { Button } from "@/components/ui/button"

export default function NotFound() {
  return (
    <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
      <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
        <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
        <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
      </div>

      <div className="text-center">
        <p className="font-serif text-8xl md:text-9xl text-primary/20">404</p>
        <Compass className="mx-auto -mt-6 size-8 text-primary" strokeWidth={1.5} />
        <h1 className="mt-6 font-serif text-2xl md:text-3xl text-balance">
          This scent has evaporated
        </h1>
        <p className="mt-3 text-muted-foreground text-balance max-w-sm mx-auto">
          We couldn&apos;t find the page you were looking for. It may have been moved or never existed.
        </p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <Button nativeButton={false} render={<Link href="/" />}>
            Back to home
          </Button>
          <Button variant="outline" nativeButton={false} render={<Link href="/perfumes" />}>
            Browse catalogue
          </Button>
        </div>
      </div>
    </main>
  )
}
