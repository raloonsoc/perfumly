"use client"

import { useLogin } from "@/lib/queries/auth";
import { useRouter } from "next/navigation";
import { LoginForm } from "@/components/login-form";

export function LoginFormClient() {
  const router = useRouter();
  const login = useLogin();

  return (
    <LoginForm isLoading={login.isPending}
      error={login.isError ? login.error?.message ?? "Something went wrong. Please try again." : null}
      onSubmit={(values) => {
        login.mutate(values, {
          onSuccess: () => router.push("/")
        })
      }}
    />
  )
}
