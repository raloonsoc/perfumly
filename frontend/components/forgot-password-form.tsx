"use client"

import * as React from "react"
import Link from "next/link"
import { CheckCircle2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Field, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { useForgotPassword } from "@/lib/queries/auth"

export function ForgotPasswordForm() {
  const forgotPassword = useForgotPassword()

  function handleSubmit(e: React.SyntheticEvent<HTMLFormElement>) {
    e.preventDefault()
    const formData = new FormData(e.currentTarget)
    const email = String(formData.get("email") ?? "")
    forgotPassword.mutate(email)
  }

  if (forgotPassword.isSuccess) {
    return (
      <div className="flex flex-col items-center gap-4 text-center">
        <div className="flex size-12 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-500">
          <CheckCircle2 className="size-6" />
        </div>
        <p className="text-sm text-muted-foreground">{forgotPassword.data.message}</p>
        <Button className="mt-2 w-full" render={<Link href="/login" />} nativeButton={false}>
          Back to login
        </Button>
      </div>
    )
  }

  return (
    <form className="flex flex-col gap-5" onSubmit={handleSubmit} noValidate>
      {forgotPassword.isError ? (
        <p role="alert" className="rounded-2xl bg-destructive/10 px-4 py-2.5 text-sm text-destructive">
          {forgotPassword.error?.message ?? "Something went wrong. Please try again."}
        </p>
      ) : null}

      <Field name="email">
        <FieldLabel>Email</FieldLabel>
        <Input
          type="email"
          name="email"
          placeholder="you@example.com"
          autoComplete="email"
          disabled={forgotPassword.isPending}
          required
        />
      </Field>

      <Button type="submit" className="mt-2 w-full" disabled={forgotPassword.isPending}>
        {forgotPassword.isPending ? "Sending…" : "Send reset link"}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Remembered your password?{" "}
        <Link href="/login" className="text-foreground underline-offset-4 hover:underline">
          Log in
        </Link>
      </p>
    </form>
  )
}
