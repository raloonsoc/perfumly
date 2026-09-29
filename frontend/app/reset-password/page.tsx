import type { Metadata } from "next"
import Link from "next/link"
import { XCircle } from "lucide-react"
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { PageTransition } from "@/components/page-transition"
import { ResetPasswordForm } from "@/components/reset-password-form"

export const metadata: Metadata = {
  title: "Reset password",
};

export default async function ResetPasswordPage({ searchParams }: PageProps<"/reset-password">) {
  const { token } = await searchParams;

  return (
    <PageTransition>
      <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
        <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
          <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
          <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
        </div>
        <Card className="w-full max-w-sm">
          {typeof token === "string" ? (
            <>
              <CardHeader className="text-center">
                <CardTitle className="font-serif text-2xl">Reset your password</CardTitle>
                <CardDescription>Choose a new password for your account</CardDescription>
              </CardHeader>
              <CardContent>
                <ResetPasswordForm token={token} />
              </CardContent>
            </>
          ) : (
            <CardHeader className="text-center justify-items-center gap-4">
              <div className="flex size-12 items-center justify-center rounded-full bg-destructive/10 text-destructive">
                <XCircle className="size-6" />
              </div>
              <CardTitle className="font-serif text-2xl">Missing token</CardTitle>
              <CardDescription>This reset link is incomplete. Please use the link from your email.</CardDescription>
              <Button className="mt-2 w-full" render={<Link href="/forgot-password" />} nativeButton={false}>
                Request a new link
              </Button>
            </CardHeader>
          )}
        </Card>
      </main>
    </PageTransition>
  );
}
