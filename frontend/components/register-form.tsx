"use client"

import * as React from "react"
import Link from "next/link"
import { Eye, EyeOff, Check, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Field, FieldLabel, FieldError } from "@/components/ui/field"
import { InputGroup, InputGroupInput, InputGroupButton } from "@/components/ui/input-group"
import { Input } from "@/components/ui/input"
import { cn } from "cn"

// Mirrors RegisterRequest's @Pattern in the backend (min 8 chars, upper, lower, digit, special char).
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^()_+\-=[\]{};:'",.<>/\\|~`]).{8,}$/

function isPasswordValid(password: string) {
  return PASSWORD_PATTERN.test(password)
}

export interface RegisterFormValues {
  username: string
  email: string
  password: string
  confirmPassword: string
}

interface RegisterFormProps {
  /** Called on submit with the raw field values; caller owns validation/request state. */
  onSubmit?: (values: RegisterFormValues) => void
  /** Disables inputs and shows a loading state on the submit button. */
  isLoading?: boolean
  /** Top-level error (e.g. "Email already in use") shown above the fields. */
  error?: string | null
  /** Per-field errors, keyed by field name. */
  fieldErrors?: Partial<Record<keyof RegisterFormValues, string>>
}

export function RegisterForm({ onSubmit, isLoading, error, fieldErrors }: RegisterFormProps) {
  const [showPassword, setShowPassword] = React.useState(false)
  const [password, setPassword] = React.useState("")
  const [confirmPassword, setConfirmPassword] = React.useState("")

  const mismatch = confirmPassword.length > 0 && password !== confirmPassword
  const passwordTouched = password.length > 0
  const passwordValid = isPasswordValid(password)

  function handleSubmit(e: React.SyntheticEvent<HTMLFormElement>) {
    e.preventDefault()
    const formData = new FormData(e.currentTarget)
    onSubmit?.({
      username: String(formData.get("username") ?? ""),
      email: String(formData.get("email") ?? ""),
      password: String(formData.get("password") ?? ""),
      confirmPassword: String(formData.get("confirmPassword") ?? ""),
    })
  }

  return (
    <form className="flex flex-col gap-5" onSubmit={handleSubmit} noValidate>
      {error ? (
        <p
          role="alert"
          className="rounded-2xl bg-destructive/10 px-4 py-2.5 text-sm text-destructive"
        >
          {error}
        </p>
      ) : null}

      <Field name="username" invalid={!!fieldErrors?.username}>
        <FieldLabel>Username</FieldLabel>
        <Input
          type="text"
          name="username"
          placeholder="jsmith"
          autoComplete="username"
          disabled={isLoading}
          required
        />
        {fieldErrors?.username ? <FieldError match>{fieldErrors.username}</FieldError> : null}
      </Field>

      <Field name="email" invalid={!!fieldErrors?.email}>
        <FieldLabel>Email</FieldLabel>
        <Input
          type="email"
          name="email"
          placeholder="you@example.com"
          autoComplete="email"
          disabled={isLoading}
          required
        />
        {fieldErrors?.email ? <FieldError match>{fieldErrors.email}</FieldError> : null}
      </Field>

      <Field name="password" invalid={(passwordTouched && !passwordValid) || !!fieldErrors?.password}>
        <FieldLabel>Password</FieldLabel>
        <InputGroup>
          <InputGroupInput
            type={showPassword ? "text" : "password"}
            name="password"
            placeholder="••••••••"
            autoComplete="new-password"
            disabled={isLoading}
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
        ) : fieldErrors?.password ? (
          <FieldError match>{fieldErrors.password}</FieldError>
        ) : null}
      </Field>

      <Field name="confirmPassword" invalid={mismatch || !!fieldErrors?.confirmPassword}>
        <FieldLabel>Confirm password</FieldLabel>
        <InputGroup>
          <InputGroupInput
            type={showPassword ? "text" : "password"}
            name="confirmPassword"
            placeholder="••••••••"
            autoComplete="new-password"
            disabled={isLoading}
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
        {mismatch ? (
          <p className="text-xs text-destructive">Passwords don&apos;t match</p>
        ) : fieldErrors?.confirmPassword ? (
          <FieldError match>{fieldErrors.confirmPassword}</FieldError>
        ) : null}
      </Field>

      <Button type="submit" className="mt-2 w-full" disabled={isLoading || mismatch || !passwordValid}>
        {isLoading ? "Creating account…" : "Create account"}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link href="/login" className="text-foreground underline-offset-4 hover:underline">
          Log in
        </Link>
      </p>
    </form>
  )
}
