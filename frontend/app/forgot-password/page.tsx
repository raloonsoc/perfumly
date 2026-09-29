import type { Metadata } from "next"
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card"
import { ForgotPasswordForm } from "@/components/forgot-password-form"
import { PageTransition } from "@/components/page-transition"

export const metadata: Metadata = {
  title: "Forgot password",
};

export default function ForgotPasswordPage() {
  return (
    <PageTransition>
      <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
        <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
          <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
          <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
        </div>
        <Card className="w-full max-w-sm">
          <CardHeader className="text-center">
            <CardTitle className="font-serif text-2xl">Forgot your password?</CardTitle>
            <CardDescription>Enter your email and we&apos;ll send you a reset link</CardDescription>
          </CardHeader>
          <CardContent>
            <ForgotPasswordForm />
          </CardContent>
        </Card>
      </main>
    </PageTransition>
  );
}
