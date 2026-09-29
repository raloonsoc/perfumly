"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useCurrentUser, type Role } from "@/lib/queries/auth"

interface UseRequireAuthOptions {
  /** If set, the user's role must match one of these, not just be logged in. */
  role?: Role | Role[]
  /** Where to send an unauthenticated (or wrong-role) user. Defaults to /login. */
  redirectTo?: string
}

// Client-side route guard: call at the top of a protected page/layout. Redirects once
// `useCurrentUser()` settles on 'error' (no session) or a role mismatch — see the
// `status`-over-`isLoading` reasoning in Navbar, which this mirrors.
export function useRequireAuth(options: UseRequireAuthOptions = {}) {
  const { role, redirectTo = "/login" } = options
  const router = useRouter()
  const { data: currentUser, status } = useCurrentUser()

  const allowedRoles = role === undefined ? undefined : Array.isArray(role) ? role : [role]
  const hasRequiredRole = allowedRoles === undefined || (!!currentUser && allowedRoles.includes(currentUser.role))

  useEffect(() => {
    if (status === "error" || (status === "success" && !hasRequiredRole)) {
      router.replace(redirectTo)
    }
  }, [status, hasRequiredRole, redirectTo, router])

  return {
    user: currentUser,
    /** True while the auth check is in flight, or once a redirect has been triggered. */
    isPending: status === "pending" || status === "error" || (status === "success" && !hasRequiredRole),
  }
}
