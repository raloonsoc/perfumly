"use client"

import * as React from "react"
import Link from "next/link"
import { CheckCircle2, XCircle, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { CardDescription, CardTitle } from "@/components/ui/card"
import { useVerifyEmail } from "@/lib/queries/auth"

interface VerifyEmailClientProps {
  token?: string
}

export function VerifyEmailClient({ token }: VerifyEmailClientProps) {
  const verifyEmail = useVerifyEmail()
  // Mutations are fire-once by default; guard against StrictMode's double-invoke
  // firing the request (and consuming the single-use token) twice.
  const hasRequested = React.useRef(false)

  React.useEffect(() => {
    if (!token || hasRequested.current) return
    hasRequested.current = true
    verifyEmail.mutate(token)
  }, [token, verifyEmail])

  if (!token) {
    return (
      <>
        <div className="flex size-12 items-center justify-center rounded-full bg-destructive/10 text-destructive">
          <XCircle className="size-6" />
        </div>
        <CardTitle className="font-serif text-2xl">Missing token</CardTitle>
        <CardDescription>This verification link is incomplete. Please use the link from your email.</CardDescription>
      </>
    )
  }

  if (verifyEmail.isPending || verifyEmail.isIdle) {
    return (
      <>
        <div className="flex size-12 items-center justify-center rounded-full bg-primary/10 text-primary">
          <Loader2 className="size-6 animate-spin" />
        </div>
        <CardTitle className="font-serif text-2xl">Verifying your email…</CardTitle>
        <CardDescription>This will only take a moment.</CardDescription>
      </>
    )
  }

  if (verifyEmail.isError) {
    return (
      <>
        <div className="flex size-12 items-center justify-center rounded-full bg-destructive/10 text-destructive">
          <XCircle className="size-6" />
        </div>
        <CardTitle className="font-serif text-2xl">Verification failed</CardTitle>
        <CardDescription>
          {verifyEmail.error?.message ?? "This link is invalid or has expired."}
        </CardDescription>
        <Button className="mt-2 w-full" render={<Link href="/register" />} nativeButton={false}>
          Back to sign up
        </Button>
      </>
    )
  }

  return (
    <>
      <div className="flex size-12 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-500">
        <CheckCircle2 className="size-6" />
      </div>
      <CardTitle className="font-serif text-2xl">Email verified</CardTitle>
      <CardDescription>Your account is now active. You can log in.</CardDescription>
      <Button className="mt-2 w-full" render={<Link href="/login" />} nativeButton={false}>
        Log in
      </Button>
    </>
  )
}
