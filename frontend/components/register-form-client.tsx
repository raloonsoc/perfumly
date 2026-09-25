"use client"

import { useRegister } from "@/lib/queries/auth";
import { useRouter } from "next/navigation";
import { RegisterForm } from "@/components/register-form";

export function RegisterFormClient() {
  const router = useRouter();
  const register = useRegister();

  return (
    <RegisterForm isLoading={register.isPending}
      error={register.isError ? register.error?.message ?? "Something went wrong. Please try again." : null}
      onSubmit={({ username, email, password }) => {
        register.mutate({ username, email, password }, {
          onSuccess: () => router.push("/")
        })
      }}
    />
  )
}
