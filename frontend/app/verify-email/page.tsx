import type { Metadata } from "next"
import {
  Card,
  CardHeader,
} from "@/components/ui/card"
import { PageTransition } from "@/components/page-transition"
import { VerifyEmailClient } from "@/components/verify-email-client"

export const metadata: Metadata = {
  title: "Verify email",
};

export default async function VerifyEmailPage({ searchParams }: PageProps<"/verify-email">) {
  const { token } = await searchParams;

  return (
    <PageTransition>
      <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
        <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
          <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
          <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
        </div>
        <Card className="w-full max-w-sm">
          <CardHeader className="text-center justify-items-center gap-4">
            <VerifyEmailClient token={typeof token === "string" ? token : undefined} />
          </CardHeader>
        </Card>
      </main>
    </PageTransition>
  );
}
