"use client"

import * as React from "react"
import Link from "next/link"
import { Eye, EyeOff, Check, X, CheckCircle2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Field, FieldLabel } from "@/components/ui/field"
import { InputGroup, InputGroupInput, InputGroupButton } from "@/components/ui/input-group"
import { cn } from "cn"
import { isPasswordValid } from "@/lib/password"
import { useResetPassword } from "@/lib/queries/auth"

interface ResetPasswordFormProps {
  token: string
}

export function ResetPasswordForm({ token }: ResetPasswordFormProps) {
  const resetPassword = useResetPassword()
  const [showPassword, setShowPassword] = React.useState(false)
  const [password, setPassword] = React.useState("")
  const [confirmPassword, setConfirmPassword] = React.useState("")

  const mismatch = confirmPassword.length > 0 && password !== confirmPassword
  const passwordTouched = password.length > 0
  const passwordValid = isPasswordValid(password)

  function handleSubmit(e: React.SyntheticEvent<HTMLFormElement>) {
    e.preventDefault()
    resetPassword.mutate({ token, newPassword: password })
  }

  if (resetPassword.isSuccess) {
    return (
      <div className="flex flex-col items-center gap-4 text-center">
        <div className="flex size-12 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-500">
          <CheckCircle2 className="size-6" />
        </div>
        <p className="text-sm text-muted-foreground">{resetPassword.data.message}</p>
        <Button className="mt-2 w-full" render={<Link href="/login" />} nativeButton={false}>
          Log in
        </Button>
      </div>
    )
  }

  return (
    <form className="flex flex-col gap-5" onSubmit={handleSubmit} noValidate>
      {resetPassword.isError ? (
        <p role="alert" className="rounded-2xl bg-destructive/10 px-4 py-2.5 text-sm text-destructive">
          {resetPassword.error?.message ?? "Something went wrong. Please try again."}
        </p>
      ) : null}

      <Field name="password" invalid={passwordTouched && !passwordValid}>
        <FieldLabel>New password</FieldLabel>
        <InputGroup>
          <InputGroupInput
            type={showPassword ? "text" : "password"}
            name="password"
            placeholder="••••••••"
            autoComplete="new-password"
            disabled={resetPassword.isPending}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            aria-invalid={passwordTouched && !passwordValid}
            required
          />
          <InputGroupButton
            type="button"
            size="icon-sm"
            aria-label={showPassword ? "Hide password" : "Show password"}
            onClick={() => setShowPassword((v) => !v)}
            className="mr-1"
          >
            {showPassword ? <EyeOff /> : <Eye />}
          </InputGroupButton>
        </InputGroup>
        {passwordTouched ? (
          <p
            className={cn(
              "flex items-center gap-1 text-xs",
              passwordValid ? "text-emerald-600 dark:text-emerald-500" : "text-destructive"
            )}
          >
            {passwordValid ? <Check className="size-3.5" /> : <X className="size-3.5" />}
            Min. 8 characters, with uppercase, lowercase, a number and a special character
          </p>
        ) : null}
      </Field>

      <Field name="confirmPassword" invalid={mismatch}>
        <FieldLabel>Confirm new password</FieldLabel>
        <InputGroup>
          <InputGroupInput
            type={showPassword ? "text" : "password"}
            name="confirmPassword"
            placeholder="••••••••"
            autoComplete="new-password"
            disabled={resetPassword.isPending}
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            aria-invalid={mismatch}
            required
          />
          <InputGroupButton
            type="button"
            size="icon-sm"
            aria-label={showPassword ? "Hide password" : "Show password"}
            onClick={() => setShowPassword((v) => !v)}
            className="mr-1"
          >
            {showPassword ? <EyeOff /> : <Eye />}
          </InputGroupButton>
        </InputGroup>
        {mismatch ? <p className="text-xs text-destructive">Passwords don&apos;t match</p> : null}
      </Field>

      <Button
        type="submit"
        className="mt-2 w-full"
        disabled={resetPassword.isPending || mismatch || !passwordValid}
      >
        {resetPassword.isPending ? "Resetting…" : "Reset password"}
      </Button>
    </form>
  )
}
