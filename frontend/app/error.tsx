"use client" // Error boundaries must be Client Components

import { useEffect } from "react"
import Link from "next/link"
import { FlaskConical } from "lucide-react"
import { Button } from "@/components/ui/button"

export default function Error({
  error,
  retry,
}: {
  error: Error & { digest?: string }
  retry: () => void
}) {
  useEffect(() => {
    console.error(error)
  }, [error])

  return (
    <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
      <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
        <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
        <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
      </div>

      <div className="text-center">
        <p className="font-serif text-8xl md:text-9xl text-destructive/20">500</p>
        <FlaskConical className="mx-auto -mt-6 size-8 text-destructive" strokeWidth={1.5} />
        <h1 className="mt-6 font-serif text-2xl md:text-3xl text-balance">
          The formula went wrong
        </h1>
        <p className="mt-3 text-muted-foreground text-balance max-w-sm mx-auto">
          Something broke on our end. Try again, or head back and pick up where you left off.
        </p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <Button onClick={() => retry()}>Try again</Button>
          <Button variant="outline" nativeButton={false} render={<Link href="/" />}>
            Back to home
          </Button>
        </div>
      </div>
    </main>
  )
}
