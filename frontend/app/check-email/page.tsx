import type { Metadata } from "next"
import { Mail } from "lucide-react"
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card"
import { PageTransition } from "@/components/page-transition"

export const metadata: Metadata = {
  title: "Check your email",
};

export default function CheckEmailPage() {
  return (
    <PageTransition>
      <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
        <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
          <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
          <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
        </div>
        <Card className="w-full max-w-sm">
          <CardHeader className="text-center justify-items-center gap-4">
            <div className="flex size-12 items-center justify-center rounded-full bg-primary/10 text-primary">
              <Mail className="size-6" />
            </div>
            <CardTitle className="font-serif text-2xl">Check your email</CardTitle>
            <CardDescription>
              We&apos;ve sent a verification link to your inbox. Click it to activate your account before logging in.
            </CardDescription>
          </CardHeader>
        </Card>
      </main>
    </PageTransition>
  );
}
