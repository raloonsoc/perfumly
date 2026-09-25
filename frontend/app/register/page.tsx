import type { Metadata } from "next"
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card"
import { PageTransition } from "@/components/page-transition"
import { RegisterFormClient } from "@/components/register-form-client";

export const metadata: Metadata = {
  title: "Sign up",
};

export default function RegisterPage() {
  return (
    <PageTransition>
      <main className="relative flex min-h-svh items-center justify-center overflow-hidden px-4 pt-24 pb-12">
        <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
          <div className="hero-glow hero-glow-center hero-glow-light grain-texture absolute inset-0" />
          <div className="hero-glow hero-glow-center hero-glow-dark absolute inset-0" />
        </div>
        <Card className="w-full max-w-sm">
          <CardHeader className="text-center">
            <CardTitle className="font-serif text-2xl">Create your account</CardTitle>
            <CardDescription>Join Perfumly to rate, review and save fragrances</CardDescription>
          </CardHeader>
          <CardContent>
            <RegisterFormClient />
          </CardContent>
        </Card>
      </main>
    </PageTransition>
  );
}
