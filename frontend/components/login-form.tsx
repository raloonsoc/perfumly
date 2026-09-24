"use client"

import * as React from "react"
import Link from "next/link"
import { Eye, EyeOff } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Field, FieldLabel, FieldError } from "@/components/ui/field"
import { InputGroup, InputGroupInput, InputGroupButton } from "@/components/ui/input-group"
import { Input } from "@/components/ui/input"

export interface LoginFormValues {
  email: string
  password: string
}

interface LoginFormProps {
  /** Called on submit with the raw field values; caller owns validation/request state. */
  onSubmit?: (values: LoginFormValues) => void
  /** Disables inputs and shows a loading state on the submit button. */
  isLoading?: boolean
  /** Top-level error (e.g. "Invalid credentials") shown above the fields. */
  error?: string | null
  /** Per-field errors, keyed by field name. */
  fieldErrors?: Partial<Record<keyof LoginFormValues, string>>
}

export function LoginForm({ onSubmit, isLoading, error, fieldErrors }: LoginFormProps) {
  const [showPassword, setShowPassword] = React.useState(false)

  function handleSubmit(e: React.SyntheticEvent<HTMLFormElement>) {
    e.preventDefault()
    const formData = new FormData(e.currentTarget)
    onSubmit?.({
      email: String(formData.get("email") ?? ""),
      password: String(formData.get("password") ?? ""),
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

      <Field name="password" invalid={!!fieldErrors?.password}>
        <div className="flex items-center justify-between">
          <FieldLabel>Password</FieldLabel>
          <Link href="#" className="text-xs text-muted-foreground hover:text-foreground">
            Forgot password?
          </Link>
        </div>
        <InputGroup>
          <InputGroupInput
            type={showPassword ? "text" : "password"}
            name="password"
            placeholder="••••••••"
            autoComplete="current-password"
            disabled={isLoading}
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
        {fieldErrors?.password ? <FieldError match>{fieldErrors.password}</FieldError> : null}
      </Field>

      <Button type="submit" className="mt-2 w-full" disabled={isLoading}>
        {isLoading ? "Signing in…" : "Sign in"}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Don&apos;t have an account?{" "}
        <Link href="/register" className="text-foreground underline-offset-4 hover:underline">
          Sign up
        </Link>
      </p>
    </form>
  )
}
