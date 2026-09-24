import type { Metadata } from "next"
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card"
import { LoginForm } from "@/components/login-form"
import { PageTransition } from "@/components/page-transition"

export const metadata: Metadata = {
  title: "Log in",
};

export default function LoginPage() {
  return (
    <PageTransition>
      <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
        <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
          <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
          <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
        </div>
        <Card className="w-full max-w-sm">
          <CardHeader className="text-center">
            <CardTitle className="font-serif text-2xl">Welcome back</CardTitle>
            <CardDescription>Log in to your Perfumly account</CardDescription>
          </CardHeader>
          <CardContent>
            <LoginForm />
          </CardContent>
        </Card>
      </main>
    </PageTransition>
  );
}
